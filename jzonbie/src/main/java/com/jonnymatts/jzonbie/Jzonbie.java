package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.jonnymatts.jzonbie.jackson.Deserializer;
import com.jonnymatts.jzonbie.jetty.JzonbieJettyServer;
import com.jonnymatts.jzonbie.pippo.PippoApplication;
import com.jonnymatts.jzonbie.pippo.PippoResponder;
import com.jonnymatts.jzonbie.priming.*;
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

import static com.jonnymatts.jzonbie.JzonbieOptions.options;

public class Jzonbie implements JzonbieClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(Jzonbie.class);

    private final PrimingContext primingContext;
    private final CallHistory callHistory = new CallHistory();
    private final List<AppRequest> failedRequests = new ArrayList<>();
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
        primingContext = new PrimingContext(options.getPriming());
        waitAfterStop = options.getWaitAfterStopping();
        objectMapper = options.getObjectMapper();
        deserializer = new Deserializer(objectMapper);
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

        LOGGER.info("Jzonbie started - HTTP port: {}{}", httpPort, httpsPort == null ? "" : ", HTTPS port: " + httpsPort);
    }

    public int getHttpPort() {
        return httpPort;
    }

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
    public ZombiePriming prime(AppRequest request, AppResponse response) {
        final ZombiePriming zombiePriming = new ZombiePriming(request, response);
        final ZombiePriming deserialized = normalizeForPriming(zombiePriming, ZombiePriming.class);
        primingContext.add(deserialized);
        return deserialized;
    }

    @Override
    public List<PrimedMapping> prime(File file) {
        try {
            final String mappingsString = IoUtils.toString(new FileInputStream(file));
            final List<PrimedMapping> primedMappings = deserializer.deserializeCollection(mappingsString, PrimedMapping.class);
            primedMappingUploader.upload(primedMappings);
            return primedMappings;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ZombiePriming prime(AppRequest request, DefaultAppResponse defaultAppResponse) {
        final AppRequest appRequest = normalizeForPriming(request, AppRequest.class);

        if(defaultAppResponse instanceof StaticDefaultAppResponse) {
            primingContext.addDefault(appRequest, normalizeStaticDefault(defaultAppResponse));
            return new ZombiePriming(request, defaultAppResponse.getResponse());
        }

        primingContext.addDefault(appRequest, defaultAppResponse);
        return new ZombiePriming(request, null);
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
    public List<ZombiePriming> getHistory() {
        return callHistory.getEntries();
    }

    @Override
    public List<AppRequest> getFailedRequests() {
        return failedRequests;
    }

    @Override
    public void verify(AppRequest appRequest, InvocationVerificationCriteria criteria) throws VerificationException {
        final int count = callHistory.count(appRequest);
        criteria.verify(count);
    }

    @Override
    public void reset() {
        primingContext.reset();
        callHistory.clear();
        failedRequests.clear();
    }

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
}