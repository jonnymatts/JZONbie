package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

public class JzonbieOptions {
    public static final int DEFAULT_PORT = 0;
    public static final String DEFAULT_ZOMBIE_HEADER_NAME = "zombie";
    public static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper().enable(INDENT_OUTPUT).setSerializationInclusion(NON_NULL);

    private int port;
    private String zombieHeaderName;
    private ObjectMapper objectMapper;

    private JzonbieOptions() {
        this.port = DEFAULT_PORT;
        this.zombieHeaderName = DEFAULT_ZOMBIE_HEADER_NAME;
        this.objectMapper = DEFAULT_OBJECT_MAPPER;
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

    public int getPort() {
        return port;
    }

    public String getZombieHeaderName() {
        return zombieHeaderName;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}