package com.frogdevelopment.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;

@Tag("integrationTest")
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = EmbeddedConfigServerBootstrapConfigurationTest.TestConfiguration.class
)
@ActiveProfiles("test")
class EmbeddedConfigServerBootstrapConfigurationTest {

    @EnableAutoConfiguration
    @Configuration
    static class TestConfiguration {

    }

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;
    @Autowired
    private ConfigServerProperties properties;
    @Autowired
    private GenericContainer embeddedConfigServer;
    @Autowired
    private ConfigClientProperties configClientProperties;

    @Test
    void embeddedConfigServerIsInstantiated() {
        var embeddedConfigServer = beanFactory.getBean(ConfigServerProperties.BEAN_NAME);
        assertThat(embeddedConfigServer).isNotNull();
    }

    @Test
    void embeddedConfigServerIsCorrectlyConfigured() {
        assertThat(embeddedConfigServer).isNotNull();
        assertThat(embeddedConfigServer.getEnvMap())
                .containsEntry("SERVER_PORT", "1234")
                .containsEntry("SPRING_CLOUD_CONFIG_SERVER_GIT_URI", properties.getGit().getUri())
                .containsEntry("SPRING_CLOUD_CONFIG_SERVER_GIT_DEFAULT_LABEL", "master")
                .containsEntry("SPRING_CLOUD_CONFIG_SERVER_GIT_TIMEOUT", "10")
                .containsEntry("SPRING_CLOUD_CONFIG_SERVER_GIT_REFRESH_RATE", "56")
                .containsEntry("SPRING_CLOUD_CONFIG_SERVER_GIT_CLONE_ON_START", "false");
        assertThat(embeddedConfigServer.getContainerInfo().getName())
                .matches("^/(testcontainer-config-server)-([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})$");
    }

    @Test
    void configClientPropertiesAreSet() {
        assertThat(configClientProperties.getUri()).doesNotContain("http://localhost:8888");
        assertThat(configClientProperties.isFailFast()).isFalse();
    }

}
