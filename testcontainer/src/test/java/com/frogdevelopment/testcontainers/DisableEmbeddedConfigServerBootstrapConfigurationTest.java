package com.frogdevelopment.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Tag("integrationTest")
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = DisableEmbeddedConfigServerBootstrapConfigurationTest.TestConfiguration.class,
        properties = {
                "testcontainer.config-server.enabled=false",
                "spring.cloud.config.enabled=false"
        }
)
@ActiveProfiles("test")
class DisableEmbeddedConfigServerBootstrapConfigurationTest {

    @EnableAutoConfiguration
    @Configuration
    static class TestConfiguration {

    }

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Autowired
    private ConfigClientProperties configClientProperties;

    @Test
    void embeddedConfigServerIsNotInstantiated() {
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> beanFactory.getBean(ConfigServerProperties.BEAN_NAME));

        assertThat(configClientProperties.isEnabled()).isFalse();
    }

}
