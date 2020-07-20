package com.frogdevelopment.testcontainers;

import static java.lang.String.valueOf;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Slf4j
@Configuration
@Order(HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "testcontainers.config-server.enabled", matchIfMissing = true)
@EnableConfigurationProperties(ConfigServerProperties.class)
public class EmbeddedConfigServerBootstrapConfiguration {

    private static final Map<Integer, MyContainer> containerCache = new HashMap<>();

    @Bean(name = ConfigServerProperties.BEAN_NAME, destroyMethod = "stop")
    public MyContainer embeddedConfigServer(ConfigServerProperties properties,
                                            ConfigClientProperties configClientProperties) {

        synchronized (containerCache) {
            var container = containerCache.get(properties.hashCode());
            if (container == null || !container.isRunning()) {
                container = new MyContainer<>(properties, containerCache::remove);

                log.info("Setup Embedded Config Server");
                setupConfigServer(properties, container);

                containerCache.put(container.getId(), container);

                log.info("Starting Testcontainers Config Server");
                container.start();
            } else {
                log.info("Re-using existing container");
            }

            log.info("Setup Config Client");
            setupConfigClient(configClientProperties, container);

            return container;

        }
    }

    private void setupConfigServer(ConfigServerProperties properties, MyContainer container) {
        var git = properties.getGit();
        container.addEnv("SPRING_CLOUD_CONFIG_SERVER_GIT_URI", git.getUri());
        container.addEnv("SPRING_CLOUD_CONFIG_SERVER_GIT_DEFAULT_LABEL", git.getBranch());
        container.addEnv("SPRING_CLOUD_CONFIG_SERVER_GIT_TIMEOUT", valueOf(git.getTimeout()));
        container.addEnv("SPRING_CLOUD_CONFIG_SERVER_GIT_REFRESH_RATE", valueOf(git.getRefreshRate()));
        container.addEnv("SPRING_CLOUD_CONFIG_SERVER_GIT_CLONE_ON_START", valueOf(git.isCloneOnStart()));
    }

    private void setupConfigClient(ConfigClientProperties configClientProperties, MyContainer container) {
        configClientProperties.setUri(new String[] {container.getUri()});
    }
}
