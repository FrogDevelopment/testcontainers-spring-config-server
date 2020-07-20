package com.frogdevelopment.testcontainers;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(ConfigServerProperties.PREFIX)
public class ConfigServerProperties {

    public static final String PREFIX = "testcontainers.config-server";
    public static final String BEAN_NAME = "embeddedSpringConfigServer";

    private boolean enabled = true;
    private Docker docker = new Docker();
    private Git git = new Git();
    private Server server = new Server();

    @Data
    public static class Docker {

        private String image = "frogdevelopment/testcontainers-config-server";
        private String tag = "Hoxton.SR6";

        public String fullName() {
            return image + ":" + tag;
        }
    }

    @Data
    public static class Git {

        private String uri;
        private String branch = "master";
        private int timeout = 15;
        private int refreshRate = 30;
        private boolean cloneOnStart = false;
    }

    @Data
    public static class Server {

        private int port = 8888;
        private String healthPath = "/actuator/health";
        private long waitTimeout = 60;

        public Duration getTimeoutDuration() {
            return Duration.ofSeconds(waitTimeout);
        }
    }
}
