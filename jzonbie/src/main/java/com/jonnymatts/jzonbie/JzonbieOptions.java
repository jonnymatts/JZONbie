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
    private static final int DEFAULT_PORT = 0;
    private static final String DEFAULT_ZOMBIE_HEADER_NAME = "zombie";
    private static final ObjectMapper DEFAULT_JSON_OBJECT_MAPPER = new JzonbieObjectMapper();
    private static final List<JzonbieRoute> DEFAULT_ROUTES = emptyList();
    private static final List<Priming> DEFAULT_PRIMING = emptyList();
    private static final int DEFAULT_CALL_HISTORY_CAPACITY = 1000;
    private static final int DEFAULT_FAILD_REQUESTS_CAPACITY = 1000;

    private int httpPort;
    private String zombieHeaderName;
    private ObjectMapper objectMapper;
    private Duration waitAfterStopping;
    private List<JzonbieRoute> routes;
    private List<Priming> priming;
    private HttpsOptions httpsOptions;
    private int callHistoryCapacity;
    private int failedRequestsCapacity;

    private JzonbieOptions() {
        this.httpPort = DEFAULT_PORT;
        this.zombieHeaderName = DEFAULT_ZOMBIE_HEADER_NAME;
        this.objectMapper = DEFAULT_JSON_OBJECT_MAPPER;
        this.routes = DEFAULT_ROUTES;
        this.priming = DEFAULT_PRIMING;
        this.callHistoryCapacity = DEFAULT_CALL_HISTORY_CAPACITY;
        this.failedRequestsCapacity = DEFAULT_FAILD_REQUESTS_CAPACITY;
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

    public JzonbieOptions withCallHistoryCapacity(int capacity) {
        this.callHistoryCapacity = capacity;
        return this;
    }

    public JzonbieOptions withFailedRequestsCapacity(int capacity) {
        this.failedRequestsCapacity = capacity;
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

    public int getCallHistoryCapacity() {
        return callHistoryCapacity;
    }

    public int getFailedRequestsCapacity() {
        return failedRequestsCapacity;
    }
}