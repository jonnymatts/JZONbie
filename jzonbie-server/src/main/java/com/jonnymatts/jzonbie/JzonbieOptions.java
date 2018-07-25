package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.jackson.JzonbieObjectMapper;
import com.jonnymatts.jzonbie.pippo.JzonbieRoute;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class JzonbieOptions {
    public static final int DEFAULT_PORT = 0;
    public static final String DEFAULT_ZOMBIE_HEADER_NAME = "zombie";
    public static final ObjectMapper DEFAULT_JSON_OBJECT_MAPPER = new JzonbieObjectMapper();
    public static final List<JzonbieRoute> DEFAULT_ROUTES = emptyList();

    private int port;
    private String zombieHeaderName;
    private ObjectMapper objectMapper;
    private Duration waitAfterStopping;
    private List<JzonbieRoute> routes;

    private JzonbieOptions() {
        this.port = DEFAULT_PORT;
        this.zombieHeaderName = DEFAULT_ZOMBIE_HEADER_NAME;
        this.objectMapper = DEFAULT_JSON_OBJECT_MAPPER;
        this.routes = DEFAULT_ROUTES;
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

    public JzonbieOptions withRoutes(List<JzonbieRoute> routes) {
        this.routes = routes;
        return this;
    }

    public JzonbieOptions withRoutes(JzonbieRoute... routes) {
        this.routes = asList(routes);
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

    public List<JzonbieRoute> getRoutes() {
        return routes;
    }
}