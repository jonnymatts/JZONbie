package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.jackson.JzonbieObjectMapper;

import java.time.Duration;
import java.util.Optional;

public class JzonbieOptions {
    public static final int DEFAULT_PORT = 0;
    public static final String DEFAULT_ZOMBIE_HEADER_NAME = "zombie";
    public static final ObjectMapper DEFAULT_JSON_OBJECT_MAPPER = new JzonbieObjectMapper();

    private int port;
    private String zombieHeaderName;
    private ObjectMapper objectMapper;
    private Duration waitAfterStopping;

    private JzonbieOptions() {
        this.port = DEFAULT_PORT;
        this.zombieHeaderName = DEFAULT_ZOMBIE_HEADER_NAME;
        this.objectMapper = DEFAULT_JSON_OBJECT_MAPPER;
    }

    public static JzonbieOptions options() {
        return new JzonbieOptions();
    }

    public JzonbieOptions withPort(int port) {
        this.port = port;
        return this;
    }

    public JzonbieOptions withZombieHeaderName(String zombieHeaderName) {
        this.zombieHeaderName = zombieHeaderName;
        return this;
    }

    public JzonbieOptions withObjectMapper(ObjectMapper mapper) {
        this.objectMapper = mapper;
        return this;
    }

    public JzonbieOptions withWaitAfterStopping(Duration waitAfterStopFor) {
        this.waitAfterStopping = waitAfterStopFor;
        return this;
    }

    public int getPort() {
        return port;
    }

    public String getZombieHeaderName() {
        return zombieHeaderName;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public Optional<Duration> getWaitAfterStopping() {
        return Optional.ofNullable(waitAfterStopping);
    }
}