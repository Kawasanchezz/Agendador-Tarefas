package com.agendador.bff.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    // JDK HttpClient (e não HttpURLConnection) porque este suporta PATCH.
    private RestClient build(String baseUrl, Duration readTimeout, String tokenInterno) {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(readTimeout);
        RestClient.Builder b = RestClient.builder().baseUrl(baseUrl).requestFactory(factory);
        if (tokenInterno != null) {
            b.defaultHeader("X-Internal-Token", tokenInterno);
        }
        return b.build();
    }

    @Bean
    @Qualifier("usuarioRestClient")
    RestClient usuarioRestClient(@Value("${services.usuario-url}") String url,
                              @Value("${internal.token}") String tokenInterno) {
        return build(url, Duration.ofSeconds(10), tokenInterno);
    }

    @Bean
    @Qualifier("tarefasRestClient")
    RestClient tarefasRestClient(@Value("${services.tarefas-url}") String url,
                              @Value("${internal.token}") String tokenInterno) {
        return build(url, Duration.ofSeconds(10), tokenInterno);
    }

    @Bean
    @Qualifier("viaCepRestClient")
    RestClient viaCepRestClient(@Value("${services.viacep-url}") String url) {
        return build(url, Duration.ofSeconds(5), null);
    }
}
