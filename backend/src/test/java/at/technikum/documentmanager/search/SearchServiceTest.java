package at.technikum.documentmanager.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    ElasticsearchClient client;

    @Test
    void usesHighlightWhenAvailable() throws Exception {
        SearchService service = new SearchService(client, "documents");

        IndexedDocument source = new IndexedDocument();
        source.setDocumentId("11111111-1111-1111-1111-111111111111");
        source.setFilename("report.pdf");
        source.setContentType("application/pdf");
        source.setUploadedAt(Instant.parse("2024-01-01T00:00:00Z"));
        source.setText("full body text");

        SearchResponse<IndexedDocument> response = SearchResponse.of(r -> r
                .hits(h -> h.hits(List.of(
                        Hit.of(hit -> hit
                                .id(source.getDocumentId())
                                .score(1.2)
                                .source(source)
                                .highlight(Map.of("text", List.of("<em>body</em> text")))
                        )
                )))
        );

        when(client.search(
                ArgumentMatchers.<SearchRequest>any(),
                ArgumentMatchers.<Class<IndexedDocument>>eq(IndexedDocument.class)))
                .thenReturn(response);

        var results = service.search("body");
        assertEquals(1, results.size());
        assertEquals("body text", results.get(0).snippet());
        assertEquals(source.getFilename(), results.get(0).filename());
        assertEquals(source.getContentType(), results.get(0).contentType());
    }
}
