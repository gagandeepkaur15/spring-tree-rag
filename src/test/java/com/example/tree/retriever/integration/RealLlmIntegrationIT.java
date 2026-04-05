package com.example.tree.retriever.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.assertj.core.api.Assertions.assertThat;

class RealLlmIntegrationIT {

    @Test
    @EnabledIfSystemProperty(named = "run.integration.tests", matches = "true")
    void integrationProfileEnabled() {
        assertThat(true).isTrue();
    }
}
