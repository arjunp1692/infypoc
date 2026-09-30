package com.sentinelflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelflow.ai.DisabledNarrativeProvider;
import com.sentinelflow.ai.NarrativeProvider;
import com.sentinelflow.ai.NarrativeValidator;
import com.sentinelflow.ai.OpenAiNarrativeProvider;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AiConfiguration {

    @Bean
    NarrativeValidator narrativeValidator(ObjectMapper objectMapper, SentinelProperties properties) {
        return new NarrativeValidator(objectMapper, properties.ai().maxSummaryLength());
    }

    @Bean
    NarrativeProvider narrativeProvider(SentinelProperties properties, ObjectMapper objectMapper) {
        if (!properties.ai().configured()) {
            return new DisabledNarrativeProvider();
        }
        Duration timeout = properties.ai().timeout();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        RestClient client = RestClient.builder()
                .baseUrl(properties.ai().baseUrl())
                .requestFactory(factory)
                .build();
        return new OpenAiNarrativeProvider(client, properties.ai(), objectMapper);
    }
}
