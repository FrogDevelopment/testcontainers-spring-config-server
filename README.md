# Testcontainers Spring Config Server
## Configuration
Add the dependency for Testcontainers Spring Config Server in your spring boot application:

```groovy
testImplementation 'com.frog-development.testcontainers:testcontainers-config-server:2020.0.2'
```

N.B.: The tag will use the [Spring Cloud Release](https://github.com/spring-projects/spring-cloud/wiki/Spring-Cloud-Hoxton-Release-Notes) tag, to easily understand what version of spring is used.

The auto-configuration will automatically launch 1 container [testcontainers-config-server](https://hub.docker.com/repository/docker/frogdevelopment/testcontainers-config-server) to be used during your integration tests.

## Customization
Here are the default values embedded by default with the dependency : 
```yaml
testcontainers:
  config-server:
    enabled: true                                             # Enable/Disable configuration of config server on startup.
    docker:
      image: frogdevelopment/testcontainers-config-server     # Docker image to use.
      tag: Hoxton.SR6                                         # Docker tag to use.
    git:
      uri: your_own_git_uri                                   # Remote Git repository to use.
      branch: the_branch_or_tag_I_want_to_use                 # Branch or tag of the repository to use.
      clone-on-start: true                                    # Flag to indicate that the repository should be cloned on startup (not on demand). Generally leads to slower startup but faster first query.
      refresh-rate: 30                                        # Time (in seconds) between refresh of the git repository, defaults to 30 seconds.
      timeout: 15                                             # Timeout (in seconds) for obtaining HTTP or SSH connection (if applicable), defaults to 5 seconds.
    server:
      port: 8888                                # Server HTTP port.
      wait-timeout: 60                          # Duration (in seconds) of waiting time until container treated as started, defaults to 60 seconds
      health-path: /actuator/health             # Path to the actuator health's endpoint, default to "/actuator/health".
```

You can override any properties by updating or adding _bootstrap-test.yml_ file in _src/test/resources_ spring project path. 

It is also possible to locally enable/disable the run of the testcontainers by setting the property `testcontainers.config-server.enabled` to false:
```java
@SpringBootTest(properties = {
        "testcontainers.config-server.enabled=false",
        "spring.cloud.config.enabled=false"
})
```
or
```java
@TestPropertySource(properties = {
        "testcontainers.config-server.enabled=false",
        "spring.cloud.config.enabled=false"
})
```

## Running with local spring-config-server instead of Testcontainers

As it can take a little more time at the beginning when testing locally, you can disable test containers and use a running locally spring cloud config server.

_bootstrap-test.yml_
```yaml
spring:
  cloud:
    config:
      uri: http://localhost:8888
      label: the_branch_or_tag_I_want_to_use

testcontainers:
  config-server:
    enabled: false
```

Here is a docker-compose config file to launch a local config server

_docker-compose.yml_
```yaml
  spring-config-server:
    container_name: spring-config-server
    image: frogdevelopment/testcontainers-config-server:2020.0.2
    ports:
      - 8888:8080
    environment:
      SPRING_CLOUD_CONFIG_SERVER_GIT_URI: your_own_git_uri
      SPRING_CLOUD_CONFIG_SERVER_GIT_DEFAULT_LABEL: develop
      SPRING_CLOUD_CONFIG_SERVER_GIT_CLONE_ON_START: 'false'
```
###### TODO
- add authentication configuration
https://cloud.spring.io/spring-cloud-config/reference/html/#_git_ssh_configuration_using_properties
