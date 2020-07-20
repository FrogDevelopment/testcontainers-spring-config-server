package com.frogdevelopment.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;

@Tag("integrationTest")
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = AnOtherContextTest.TestConfiguration.class
)
@ActiveProfiles("test")
class AnOtherContextTest {

    @EnableAutoConfiguration
    @Configuration
    static class TestConfiguration {

    }

    @Autowired
    private GenericContainer embeddedConfigServer;

    @Test
    void embeddedConfigServerIsInstantiated() {
        assertThat(embeddedConfigServer).isNotNull();
    }

}
