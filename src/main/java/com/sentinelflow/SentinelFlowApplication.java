package com.sentinelflow;

import com.sentinelflow.config.SentinelProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SentinelProperties.class)
public class SentinelFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentinelFlowApplication.class, args);
    }
}
