package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.jonnymatts.jzonbie.defaults.Priming;
import com.jonnymatts.jzonbie.history.CallHistory;
import com.jonnymatts.jzonbie.history.Exchange;
import com.jonnymatts.jzonbie.history.FixedCapacityCache;
import com.jonnymatts.jzonbie.jackson.Deserializer;
import com.jonnymatts.jzonbie.jetty.JzonbieJettyServer;
import com.jonnymatts.jzonbie.logging.Logging;
import com.jonnymatts.jzonbie.pippo.PippoApplication;
import com.jonnymatts.jzonbie.pippo.PippoResponder;
import com.jonnymatts.jzonbie.priming.AppRequestFactory;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.priming.PrimingContext;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.requests.AppRequestHandler;
import com.jonnymatts.jzonbie.requests.PrimedMappingUploader;
import com.jonnymatts.jzonbie.requests.ZombieRequestHandler;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.CurrentPrimingFileResponseFactory;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;
import com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse;
import com.jonnymatts.jzonbie.ssl.HttpsSupport;
import com.jonnymatts.jzonbie.templating.JzonbieHandlebars;
import com.jonnymatts.jzonbie.templating.ResponseTransformer;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import com.jonnymatts.jzonbie.verification.VerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Pippo;
import ro.pippo.core.WebServerSettings;
import ro.pippo.core.util.IoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ch.qos.logback.classic.Level.ERROR;
import static ch.qos.logback.classic.Level.OFF;
import static com.jonnymatts.jzonbie.JzonbieOptions.options;
import static com.jonnymatts.jzonbie.defaults.DefaultResponsePriming.defaultPriming;
import static com.jonnymatts.jzonbie.defaults.StandardPriming.priming;
import static java.util.Collections.emptyList;

/**
 * Class that provide a mock HTTP(S) server.
 * <p>
 * A Jzonbie can be primed to return responses when an incoming requests are matched.
 * <pre>
 * {@code
 * final Jzonbie jzonbie = new Jzonbie();
 *
 * jzonbie.prime(
 *      get("/"),
 *      ok()
 * );
 * }
 * </pre>
 * A Jzonbie can be configured with {@link JzonbieOptions}.
 * <pre>
 * {@code
 * final Jzonbie jzonbie = new Jzonbie(
 *      options()
 *          .withHttpPort(9000)
 *          .withZombieHeaderName("header")
 *          .withRoutes(
 *              post("/action", ctx -> ctx.getRouteContext().getResponse().status(202).commit())
 *          )
 *      );
 * }
 * </pre>
 */
public class Jzonbie implements JzonbieClient {

    static {
        Logging.setLevel("org.eclipse", ERROR);
        Logging.setLevel("ro.pippo", OFF);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Jzonbie.class);

    private final PrimingContext primingContext;
    private final CallHistory callHistory;
    private final FixedCapacityCache<AppRequest> failedRequests;
    private final int httpPort;
    private final Integer httpsPort;
    private final Pippo httpPippo;
    private final Pippo httpsPippo;
    private final HttpsSupport httpsSupport;
    private Deserializer deserializer;
    private ObjectMapper objectMapper;
    private PrimedMappingUploader primedMappingUploader;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Duration> waitAfterStop;

    public Jzonbie() {
        this(options());
    }

    public Jzonbie(JzonbieOptions options) {
        this.httpsSupport = new HttpsSupport();
        callHistory = new CallHistory(options.getCallHistoryCapacity());
        failedRequests = new FixedCapacityCache<>(options.getFailedRequestsCapacity());
        waitAfterStop = options.getWaitAfterStopping();
        objectMapper = options.getObjectMapper();
        deserializer = new Deserializer(objectMapper);
        primingContext = new PrimingContext(getDefaultPriming(options));
        final AppRequestFactory appRequestFactory = new AppRequestFactory(deserializer);
        final CurrentPrimingFileResponseFactory fileResponseFactory = new CurrentPrimingFileResponseFactory(objectMapper);
        primedMappingUploader = new PrimedMappingUploader(primingContext);
        final AppRequestHandler appRequestHandler = new AppRequestHandler(primingContext, callHistory, failedRequests, appRequestFactory);
        final ZombieRequestHandler zombieRequestHandler = new ZombieRequestHandler(options.getZombieHeaderName(), primingContext, callHistory, failedRequests, deserializer, fileResponseFactory, primedMappingUploader, httpsSupport);

        options.getRoutes().forEach(route -> {
            route.setJzonbieClient(this);
            route.setDeserializer(deserializer);
        });

        final Handlebars handlebars = new JzonbieHandlebars();
        final ResponseTransformer responseTransformer = new ResponseTransformer(handlebars);
        final PippoResponder pippoResponder = new PippoResponder(responseTransformer, objectMapper);

        final PippoApplication application = new PippoApplication(options.getZombieHeaderName(), options.getRoutes(), appRequestHandler, zombieRequestHandler, pippoResponder);

        httpPippo = createPippo(application, options.getHttpPort());
        httpPippo.start();
        httpPort = httpPippo.getServer().getPort();

        if(options.getHttpsOptions().isPresent()) {
            final HttpsOptions httpsOptions = options.getHttpsOptions().get();
            httpsPippo = createPippo(application, httpsOptions.getPort());
            configureHttps(httpsPippo, httpsOptions);
            httpsPippo.start();
            httpsPort = httpsPippo.getServer().getPort();
        } else {
            httpsPippo = null;
            httpsPort = null;
        }

        options.getInitialPrimingFile().ifPresent(this::prime);

        LOGGER.info("Jzonbie started - HTTP port: {}{}", httpPort, httpsPort == null ? "" : ", HTTPS port: " + httpsPort);
    }

    /**
     * Returns the port that Jzonbie is serving HTTP traffic on.
     *
     * @return Jzonbie HTTP port
     */
    public int getHttpPort() {
        return httpPort;
    }

    /**
     * Returns the port that Jzonbie is serving HTTPS traffic on.
     *
     * @throws IllegalStateException if HTTPS is not configured
     * @return Jzonbie HTTPS port
     */
    public int getHttpsPort() {
        if(httpsPort == null) {
            throw new IllegalStateException("No https server configured");
        }
        return httpsPort;
    }

    @Override
    public KeyStore getTruststore() {
        return httpsSupport.getTrustStore();
    }

    @Override
    public void prime(AppRequest request, AppResponse response) {
        final ZombiePriming zombiePriming = new ZombiePriming(request, response);
        final ZombiePriming deserialized = normalizeForPriming(zombiePriming, ZombiePriming.class);
        primingContext.add(deserialized);
    }

    @Override
    public void prime(File file) {
        primedMappingUploader.upload(getPrimedMappingsFromFile(file));
    }

    @Override
    public void prime(AppRequest request, DefaultAppResponse defaultAppResponse) {
        final AppRequest appRequest = normalizeForPriming(request, AppRequest.class);

        if(defaultAppResponse instanceof StaticDefaultAppResponse) {
            primingContext.addDefault(appRequest, normalizeStaticDefault(defaultAppResponse));
        }

        primingContext.addDefault(appRequest, defaultAppResponse);
    }

    private DefaultAppResponse normalizeStaticDefault(DefaultAppResponse defaultAppResponse) {
        return normalizeForPriming(defaultAppResponse, StaticDefaultAppResponse.class);
    }

    private <T> T normalizeForPriming(T appRequest, Class<? extends T> clazz) {
        try {
            return deserializer.deserialize(objectMapper.writeValueAsString(appRequest), clazz);
        } catch(JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PrimedMapping> getCurrentPriming() {
        return primingContext.getCurrentPriming();
    }

    @Override
    public List<Exchange> getHistory() {
        return callHistory.getValues();
    }

    @Override
    public List<AppRequest> getFailedRequests() {
        return failedRequests.getValues();
    }

    @Override
    public void verify(AppRequest request, InvocationVerificationCriteria criteria) throws VerificationException {
        final int count = callHistory.count(request);
        criteria.verify(count);
    }

    @Override
    public void reset() {
        primingContext.reset();
        callHistory.clear();
        failedRequests.clear();
    }

    /**
     * Stops Jzonbie HTTP(S) server(s).
     */
    public void stop() {
        httpPippo.stop();
        if(httpsPippo != null) {
            httpsPippo.stop();
        }
        waitAfterStop.ifPresent(wait -> {
            try {
                Thread.sleep(wait.toMillis());
            } catch (InterruptedException ignored) {}
        });
    }

    private Pippo createPippo(PippoApplication application, int port) {
        final Pippo pippo = new Pippo(application);
        final JzonbieJettyServer server = new JzonbieJettyServer();
        pippo.setServer(server);
        server.setPort(port);
        final WebServerSettings settings = server.getSettings();
        settings.host("0.0.0.0");
        return pippo;
    }

    private void configureHttps(Pippo pippo, HttpsOptions httpsOptions) {
        final WebServerSettings settings = pippo.getServer().getSettings();

        final Optional<String> keystoreLocation = httpsOptions.getKeystoreLocation();
        final Optional<String> keystorePassword = httpsOptions.getKeystorePassword();

        if(!keystoreLocation.isPresent()) {
            httpsSupport.createKeystoreAndTruststore("/tmp/jzonbie.jks", httpsOptions.getCommonName());
            settings.keystoreFile("/tmp/jzonbie.jks");
            settings.keystorePassword("jzonbie");
        } else {
            settings.keystoreFile(keystoreLocation.get());
            keystorePassword.ifPresent(settings::keystorePassword);
        }
    }

    private List<Priming> getDefaultPriming(JzonbieOptions options) {
        final List<Priming> primingFromOptions = options.getPriming();
        final List<Priming> primingFromFile = options.getDefaultPrimingFile().map(this::getDefaultPrimingFromFile).orElse(emptyList());
        final List<Priming> priming = new ArrayList<>(primingFromOptions);
        priming.addAll(primingFromFile);
        return priming;
    }

    private List<Priming> getDefaultPrimingFromFile(File file) {
        final List<Priming> priming = new ArrayList<>();
        final List<PrimedMapping> primedMappings = getPrimedMappingsFromFile(file);
        for (PrimedMapping primedMapping : primedMappings) {
            final AppRequest request = primedMapping.getRequest();
            primedMapping.getResponses().getPrimed().forEach(response -> priming.add(priming(request, response)));
            primedMapping.getResponses().getDefault().ifPresent(defaultResponse -> priming.add(defaultPriming(request, defaultResponse)));
        }
        return priming;
    }

    private List<PrimedMapping> getPrimedMappingsFromFile(File file) {
        try {
            final String mappingsString = IoUtils.toString(new FileInputStream(file));
            return deserializer.deserializeCollection(mappingsString, PrimedMapping.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}