package at.technikum.documentmanager.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final String indexName;

    public SearchService(
            ElasticsearchClient elasticsearchClient,
            @Value("${elasticsearch.index:documents}") String indexName
    ) {
        this.elasticsearchClient = elasticsearchClient;
        this.indexName = indexName;
    }

    public List<SearchResult> search(String query) {
        try {
            SearchResponse<IndexedDocument> response = elasticsearchClient.search(s -> s
                            .index(indexName)
                            .size(20)
                            .query(q -> q.multiMatch(m -> m
                                    .fields("text")
                                    .fields("filename")
                                    .fields("summary")
                                    .query(query)
                            ))
                            .highlight(h -> h
                                    .fields("text", f -> f.fragmentSize(160).numberOfFragments(1))
                                    .fields("summary", f -> f.fragmentSize(160).numberOfFragments(1))
                            ),
                    IndexedDocument.class);

            return response.hits().hits().stream()
                    .map(this::mapHit)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to search documents", e);
        }
    }

    private SearchResult mapHit(Hit<IndexedDocument> hit) {
        IndexedDocument source = hit.source();
        String snippet = buildSnippet(hit, source);
        UUID id = parseUuid(hit.id());
        return new SearchResult(
                id,
                source != null ? source.getFilename() : null,
                source != null ? source.getContentType() : null,
                source != null ? source.getUploadedAt() : null,
                snippet,
                hit.score() != null ? hit.score() : 0.0
        );
    }

    private String buildSnippet(Hit<IndexedDocument> hit, IndexedDocument source) {
        Map<String, List<String>> highlights = hit.highlight();
        if (highlights != null) {
            List<String> textFragments = highlights.get("text");
            if (textFragments != null && !textFragments.isEmpty()) {
                return stripTags(textFragments.get(0));
            }
            List<String> summaryFragments = highlights.get("summary");
            if (summaryFragments != null && !summaryFragments.isEmpty()) {
                return stripTags(summaryFragments.get(0));
            }
        }
        if (source != null && source.getSummary() != null && !source.getSummary().isBlank()) {
            return trim(source.getSummary());
        }
        if (source != null && source.getText() != null && !source.getText().isBlank()) {
            return trim(source.getText());
        }
        return "";
    }

    private String stripTags(String value) {
        return value.replaceAll("<[^>]+>", "");
    }

    private String trim(String value) {
        return value.length() > 200 ? value.substring(0, 200) + "..." : value;
    }

    private UUID parseUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception e) {
            return null;
        }
    }
}
