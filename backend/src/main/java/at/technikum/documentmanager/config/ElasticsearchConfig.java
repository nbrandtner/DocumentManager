package at.technikum.documentmanager.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.url:http://localhost:9200}")
    private String elasticUrl;

    @Bean(destroyMethod = "close")
    public RestClient restClient() {
        return RestClient.builder(HttpHost.create(elasticUrl)).build();
    }

    @Bean(destroyMethod = "close")
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient, ObjectMapper mapper) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper(mapper));
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

    @Bean
    public String elasticsearchIndex(@Value("${elasticsearch.index:documents}") String index) {
        return index;
    }
}
