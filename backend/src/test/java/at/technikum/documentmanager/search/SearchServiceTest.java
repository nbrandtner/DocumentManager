package at.technikum.documentmanager.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.util.ObjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
                .took(1)
                .timedOut(false)
                .shards(s -> s.total(1).successful(1).failed(0))
                .hits(h -> h.hits(List.of(
                        Hit.of(hit -> hit
                                .index("documents")
                                .id(source.getDocumentId())
                                .score(1.2)
                                .source(source)
                                .highlight(Map.of("text", List.of("<em>body</em> text")))
                        )
                )))
        );

        when(client.search(
                ArgumentMatchers.<Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>>>any(),
                eq(IndexedDocument.class)
        )).thenReturn(response);

        var results = service.search("body");
        assertEquals(1, results.size());
        assertEquals("body text", results.getFirst().snippet());
        assertEquals(source.getFilename(), results.getFirst().filename());
        assertEquals(source.getContentType(), results.getFirst().contentType());
    }
}
