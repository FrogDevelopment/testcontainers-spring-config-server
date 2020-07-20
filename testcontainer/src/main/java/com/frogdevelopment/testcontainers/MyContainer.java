package com.frogdevelopment.testcontainers;

import static java.lang.String.format;
import static java.lang.String.valueOf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frogdevelopment.testcontainers.ConfigServerProperties.Server;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.rnorth.ducttape.ratelimits.RateLimiterBuilder;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

@Slf4j
public class MyContainer<SELF extends MyContainer<SELF>> extends GenericContainer<SELF> {

    @Getter
    private final int id;
    private final String simpleDockerName;
    private final Server server;
    private final Consumer<Integer> closeCallback;

    public MyContainer(@NonNull ConfigServerProperties properties,
                       Consumer<Integer> closeCallback) {

        super(properties.getDocker().fullName());

        this.id = properties.hashCode();
        this.simpleDockerName = simplifyDockerImageName(properties.getDocker().getImage());
        this.server = properties.getServer();
        this.closeCallback = closeCallback;

        log.info("Creating testcontainer: {}", simpleDockerName);

        withEnv("SERVER_PORT", valueOf(server.getPort()));
        withExposedPorts(server.getPort());

        withLogConsumer(getLogConsumer());

        withCreateContainerCmdModifier(this::setContainerName);
        waitingFor(waitForUp(server));
    }

    @Override
    public void close() {
        try {
            super.close();
            closeCallback.accept(id);
        } catch (Exception e) {
            log.error("Error while closing", e);
        }
    }

    public String getUri() {
        var host = getContainerIpAddress();
        var mappedPort = getMappedPort(server.getPort());
        return format("http://%s:%s", host, mappedPort);
    }

    @Override
    protected void containerIsStopping(InspectContainerResponse containerInfo) {
        log.info("Stopping container {}", containerInfo.getName());
    }

    @Override
    protected void containerIsStopped(InspectContainerResponse containerInfo) {
        log.info("Container {} stopped", containerInfo.getName());
    }

    @NotNull
    private Slf4jLogConsumer getLogConsumer() {
        return new Slf4jLogConsumer(LoggerFactory.getLogger(simpleDockerName)).withSeparateOutputStreams();
    }

    @NotNull
    private static String simplifyDockerImageName(String dockerImage) {
        var simpleDockerName = dockerImage;
        if (dockerImage.contains("/")) {
            var bits = dockerImage.split("/");
            simpleDockerName = bits[bits.length - 1];
        }
        return simpleDockerName;
    }

    private void setContainerName(CreateContainerCmd createContainerCmd) {
        createContainerCmd.withHostName(simpleDockerName);
        createContainerCmd.withName(simpleDockerName + "-" + UUID.randomUUID().toString());
    }

    private WaitStrategy waitForUp(Server server) {
        return new HttpWaitStrategy()
                .forPort(server.getPort())
                .forPath(server.getHealthPath())
                .forStatusCode(200)
                .forResponsePredicate(this::checkStatus)
                .withRateLimiter(RateLimiterBuilder
                        .newBuilder()
                        .withRate(5, TimeUnit.SECONDS)
                        .withConstantThroughput()
                        .build())
                .withStartupTimeout(server.getTimeoutDuration());
    }

    private boolean checkStatus(String status) {
        try {
            log.info("Current status = {}", status);
            return HealthStatus.fromString(status).isUp();
        } catch (JsonProcessingException e) {
            log.error("Incorrect json", e);
            return false;
        }
    }

    @Data
    private static class HealthStatus {

        private static final ObjectMapper objectMapper = new ObjectMapper();

        private String status;

        private boolean isUp() {
            return "UP".equalsIgnoreCase(status);
        }

        private static HealthStatus fromString(String value) throws JsonProcessingException {
            return objectMapper.readValue(value, HealthStatus.class);
        }
    }
}
