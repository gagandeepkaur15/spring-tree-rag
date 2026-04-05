package com.example.tree.retriever.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class TreeRetrieverAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TreeRetrieverAutoConfiguration.class));

    @Test
    void autoConfigurationLoadsSuccessfully() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(TreeRetrieverAutoConfiguration.class);
        });
    }

    @Test
    void createsRetrieverBeansWhenChatClientExists() {
        contextRunner
                .withUserConfiguration(ChatClientConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(ChatClient.class);
                    assertThat(context).hasBean("documentRetriever");
                    assertThat(context).hasBean("treeRetrieverEndpoint");
                });
    }

    @Configuration(proxyBeanMethods = false)
    @AutoConfiguration
    static class ChatClientConfig {
        @Bean
        ChatClient chatClient() {
            return mock(ChatClient.class);
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
