package com.example.urlshortener.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceClientConfigurationBuilderCustomizer redisClientCustomizer() {
        return builder -> {
            SocketOptions socketOptions = SocketOptions.builder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            ClientOptions clientOptions = ClientOptions.builder()
                    .socketOptions(socketOptions)
                    .build();
            builder.clientOptions(clientOptions)
                    .commandTimeout(Duration.ofSeconds(10));
        };
    }
}
