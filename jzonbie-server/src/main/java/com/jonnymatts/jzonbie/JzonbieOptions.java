package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.defaults.Priming;
import com.jonnymatts.jzonbie.jackson.JzonbieObjectMapper;
import com.jonnymatts.jzonbie.pippo.JzonbieRoute;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.jonnymatts.jzonbie.HttpsOptions.httpsOptions;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class JzonbieOptions {
    public static final int DEFAULT_PORT = 0;
    public static final String DEFAULT_ZOMBIE_HEADER_NAME = "zombie";
    public static final ObjectMapper DEFAULT_JSON_OBJECT_MAPPER = new JzonbieObjectMapper();
    public static final List<JzonbieRoute> DEFAULT_ROUTES = emptyList();
    public static final List<Priming> DEFAULT_PRIMING = emptyList();

    private int httpPort;
    private String zombieHeaderName;
    private ObjectMapper objectMapper;
    private Duration waitAfterStopping;
    private List<JzonbieRoute> routes;
    private List<Priming> priming;
    private HttpsOptions httpsOptions;

    private JzonbieOptions() {
        this.httpPort = DEFAULT_PORT;
        this.zombieHeaderName = DEFAULT_ZOMBIE_HEADER_NAME;
        this.objectMapper = DEFAULT_JSON_OBJECT_MAPPER;
        this.routes = DEFAULT_ROUTES;
        this.priming = DEFAULT_PRIMING;
    }

    public static JzonbieOptions options() {
        return new JzonbieOptions();
    }

    public JzonbieOptions withHttpPort(int httpPort) {
        this.httpPort = httpPort;
        return this;
    }

    public JzonbieOptions withHttps(HttpsOptions httpsOptions) {
        this.httpsOptions = httpsOptions;
        return this;
    }

    public JzonbieOptions withHttps() {
        this.httpsOptions = httpsOptions();
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

    public JzonbieOptions withPriming(Priming... priming) {
        this.priming = asList(priming);
        return this;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public String getZombieHeaderName() {
        return zombieHeaderName;
    }

    public Optional<HttpsOptions> getHttpsOptions() { return Optional.ofNullable(httpsOptions); }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public Optional<Duration> getWaitAfterStopping() {
        return Optional.ofNullable(waitAfterStopping);
    }

    public List<JzonbieRoute> getRoutes() {
        return routes;
    }

    public List<Priming> getPriming() {
        return priming;
    }
}