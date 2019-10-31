package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.defaults.Priming;
import com.jonnymatts.jzonbie.jackson.JzonbieObjectMapper;
import com.jonnymatts.jzonbie.pippo.JzonbieRoute;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.jonnymatts.jzonbie.HttpsOptions.httpsOptions;
import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * Class that defines the configuration of a custom Jzonbie.
 * <p>
 * By default, a Jzonbie will be configured to serve HTTP traffic on a
 * random, available port. The Jzonbie functions can be accessed over
 * HTTP using the header "zombie".
 * <pre>
 * {@code
 * options().withHttpPort(9000).withZombieHeaderName("other")
 * }
 * </pre>
 * Jzonbie can be configured with {@link Priming} that will be set when
 * started and reset.
 * <pre>
 * {@code
 * options().withPriming(
 *      priming(get("/"), ok()),
 *      defaultPriming(post("/create"), staticDefault(created()))
 * )
 * }
 * </pre>
 * It can also be configured with custom {@link JzonbieRoute} endpoints.
 * <pre>
 * {@code
 * options().withRoutes(
 *      get("/ready", ctx -> ctx.getRouteContext().getResponse().ok())
 * )
 * }
 * </pre>
 */
public class JzonbieOptions {
    private static final int DEFAULT_PORT = 0;
    private static final String DEFAULT_ZOMBIE_HEADER_NAME = "zombie";
    private static final ObjectMapper DEFAULT_JSON_OBJECT_MAPPER = new JzonbieObjectMapper();
    private static final List<JzonbieRoute> DEFAULT_ROUTES = emptyList();
    private static final List<Priming> DEFAULT_PRIMING = emptyList();
    private static final int DEFAULT_CALL_HISTORY_CAPACITY = 1000;
    private static final int DEFAULT_FAILED_REQUESTS_CAPACITY = 1000;
    private static final String DEFAULT_HOME_PATH = getProperty("user.home");

    private int httpPort;
    private String zombieHeaderName;
    private ObjectMapper objectMapper;
    private Duration waitAfterStopping;
    private List<JzonbieRoute> routes;
    private List<Priming> priming;
    private HttpsOptions httpsOptions;
    private int callHistoryCapacity;
    private int failedRequestsCapacity;
    private File initialPrimingFile;
    private File defaultPrimingFile;
    private String homePath;

    private JzonbieOptions() {
        this.httpPort = DEFAULT_PORT;
        this.zombieHeaderName = DEFAULT_ZOMBIE_HEADER_NAME;
        this.objectMapper = DEFAULT_JSON_OBJECT_MAPPER;
        this.routes = DEFAULT_ROUTES;
        this.priming = DEFAULT_PRIMING;
        this.callHistoryCapacity = DEFAULT_CALL_HISTORY_CAPACITY;
        this.failedRequestsCapacity = DEFAULT_FAILED_REQUESTS_CAPACITY;
        this.homePath = DEFAULT_HOME_PATH;
    }

    /**
     * Returns the default Jzonbie configuration.
     *
     * @return default Jzonbie configuration
     */
    public static JzonbieOptions options() {
        return new JzonbieOptions();
    }

    /**
     * Configures Jzonbie to serve HTTP traffic on the given port.
     * <p>
     * By default Jzonbie will choose a random, available port.
     *
     * @param httpPort HTTP port
     * @return this Jzonbie configuration with a configured port
     */
    public JzonbieOptions withHttpPort(int httpPort) {
        this.httpPort = httpPort;
        return this;
    }

    /**
     * Configures Jzonbie to serve HTTPS traffic with the given configuration.
     *
     * @param httpsOptions HTTPS configuration
     * @return this Jzonbie configuration with HTTPS configuration
     */
    public JzonbieOptions withHttps(HttpsOptions httpsOptions) {
        this.httpsOptions = httpsOptions;
        return this;
    }

    /**
     * Configures Jzonbie to serve HTTPS traffic with the default configuration.
     *
     * @return this Jzonbie configuration with default HTTPS configuration
     */
    public JzonbieOptions withHttps() {
        this.httpsOptions = httpsOptions();
        return this;
    }

    /**
     * Configures Jzonbie to provide access to functions over HTTP using the given header.
     *
     * @param zombieHeaderName header name
     * @return this Jzonbie configuration with HTTPS configuration
     */
    public JzonbieOptions withZombieHeaderName(String zombieHeaderName) {
        this.zombieHeaderName = zombieHeaderName;
        return this;
    }

    /**
     * Configures Jzonbie to (de)serialize with the given {@link ObjectMapper}.
     * <p>
     * By default Jzonbie will use {@link JzonbieObjectMapper}.
     *
     * @param mapper {@link ObjectMapper}
     * @return this Jzonbie configuration with a configured {@link ObjectMapper}
     */
    public JzonbieOptions withObjectMapper(ObjectMapper mapper) {
        this.objectMapper = mapper;
        return this;
    }

    /**
     * Configures Jzonbie to wait for a given duration after it is stopped.
     * <p>
     * This is useful when a Jzonbie is repeatedly restarted using the same port.
     *
     * @param waitAfterStopFor wait duration
     * @return this Jzonbie configuration with a configured duration to wait after stopping
     */
    public JzonbieOptions withWaitAfterStopping(Duration waitAfterStopFor) {
        this.waitAfterStopping = waitAfterStopFor;
        return this;
    }

    /**
     * Configures Jzonbie with the given custom endpoints.
     *
     * @param routes custom endpoints
     * @return this Jzonbie configuration with custom endpoints
     */
    public JzonbieOptions withRoutes(List<JzonbieRoute> routes) {
        this.routes = routes;
        return this;
    }

    /**
     * Configures Jzonbie with the given custom endpoints.
     *
     * @param routes custom endpoints
     * @return this Jzonbie configuration with custom endpoints
     */
    public JzonbieOptions withRoutes(JzonbieRoute... routes) {
        this.routes = asList(routes);
        return this;
    }

    /**
     * Configures Jzonbie with the given default priming.
     * <p>
     * The priming will be set on start and reset.
     *
     * @param priming default
     * @return this Jzonbie configuration with default priming
     */
    public JzonbieOptions withPriming(Priming... priming) {
        this.priming = asList(priming);
        return this;
    }

    /**
     * Configures max capacity of Jzonbie call history cache.
     * <p>
     * By default Jzonbie will have a call history capacity of <b>1000</b>.
     *
     * @param capacity call history capacity
     * @return this Jzonbie configuration with the given call history capacity
     */
    public JzonbieOptions withCallHistoryCapacity(int capacity) {
        this.callHistoryCapacity = capacity;
        return this;
    }

    /**
     * Configures max capacity of Jzonbie failed requests cache.
     * <p>
     * By default Jzonbie will have a failed request capacity of <b>1000</b>.
     *
     * @param capacity failed request capacity
     * @return this Jzonbie configuration with the given failed request capacity
     */
    public JzonbieOptions withFailedRequestsCapacity(int capacity) {
        this.failedRequestsCapacity = capacity;
        return this;
    }

    /**
     * Specifies a JSON file containing priming that will be applied on Jzonbie start-up.
     *
     * @param initialPrimingFile the JSON priming file
     * @return this Jzonbie configuration with initial priming file
     * @see Jzonbie#prime(File)
     */
    public JzonbieOptions withInitialPrimingFile(File initialPrimingFile) {
        this.initialPrimingFile = initialPrimingFile;
        return this;
    }

    /**
     * Specifies a JSON file containing default priming that will be applied on Jzonbie start-up.
     *
     * @param defaultPrimingFile the JSON default priming file
     * @return this Jzonbie configuration with default priming file
     */
    public JzonbieOptions withDefaultPrimingFile(File defaultPrimingFile) {
        this.defaultPrimingFile = defaultPrimingFile;
        return this;
    }

    /**
     * Specifies the location of the '.jzonbie' home folder where Jzonbie context is stored.
     *
     * If not specified will default to '~/'
     *
     * @param homePath the Jzonbie home path
     * @return this Jzonbie configuration with configured home path
     */
    public JzonbieOptions withHomePath(String homePath) {
        this.homePath = homePath;
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

    public Optional<File> getInitialPrimingFile() {
        return Optional.ofNullable(initialPrimingFile);
    }

    public Optional<File> getDefaultPrimingFile() {
        return Optional.ofNullable(defaultPrimingFile);
    }

    public String getHomePath() {
        return homePath;
    }
}