package com.jonnymatts.jzonbie;

import com.google.common.base.Stopwatch;
import com.jonnymatts.jzonbie.client.ApacheJzonbieHttpClient;
import com.jonnymatts.jzonbie.junit.JzonbieExtension;
import com.jonnymatts.jzonbie.junit.TestJzonbie;
import com.jonnymatts.jzonbie.pippo.JzonbieRoute;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DynamicDefaultAppResponse;
import com.jonnymatts.jzonbie.verification.VerificationException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;
import static com.jonnymatts.jzonbie.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.body.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.defaults.DefaultResponsePriming.defaultPriming;
import static com.jonnymatts.jzonbie.defaults.StandardPriming.priming;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.requests.AppRequest.post;
import static com.jonnymatts.jzonbie.responses.AppResponse.internalServerError;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.responses.defaults.DynamicDefaultAppResponse.dynamicDefault;
import static com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse.staticDefault;
import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.equalTo;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(JzonbieExtension.class)
class JzonbieTest {

    private HttpUriRequest httpRequest;
    private HttpClient client;

    @BeforeEach
    void setUp(Jzonbie jzonbie) {
        httpRequest = RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + "/").build();

        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(10);
        connectionManager.setDefaultMaxPerRoute(10);
        client = HttpClientBuilder.create().setConnectionManager(connectionManager).build();
    }

    @Test
    void jzonbieCanBePrimed(Jzonbie jzonbie) {
        final AppRequest request = get("/");
        final AppResponse response = ok().withBody(objectBody(singletonMap("key", "val")));
        jzonbie.prime(request, response);

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(request);
        assertThat(primedMapping.getResponses().getPrimed()).containsOnly(response);
    }

    @Test
    void jzonbieCanBePrimedWithStringBodyContent(Jzonbie jzonbie) {
        final AppRequest request = post("/").withBody(stringBody("<jzonbie>message</jzonbie>"));
        final AppResponse response = ok().withBody(stringBody("<response>message</response>"));
        jzonbie.prime(request, response);

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(request);
        assertThat(primedMapping.getResponses().getPrimed()).containsOnly(response);
    }

    @Test
    void jzonbieCanBePrimedWithListBodyContent(Jzonbie jzonbie) {
        final AppRequest request = post("/").withBody(arrayBody(singletonList("request")));
        final AppResponse response = ok().withBody(arrayBody(singletonList("response")));
        jzonbie.prime(request, response);

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(request);
        assertThat(primedMapping.getResponses().getPrimed()).containsOnly(response);
    }

    @Test
    void jzonbieCanBePrimedWithJsonStringListBodyContent(Jzonbie jzonbie) {
        final AppRequest request = post("/").withBody(stringBody("request"));
        final AppResponse response = ok().withBody(stringBody("response"));
        jzonbie.prime(request, response);

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(request);
        assertThat(primedMapping.getResponses().getPrimed()).containsOnly(response);
    }

    @Test
    void jzonbieCanBePrimedForStaticDefault(Jzonbie jzonbie) {
        final AppRequest request = get("/");
        final AppResponse response = ok();
        jzonbie.prime(request, staticDefault(response));

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(request);
        assertThat(primedMapping.getResponses().getDefault().map(DefaultAppResponse::getResponse)).contains(response);
    }

    @Test
    void jzonbieCanBePrimedForDynamicDefault(Jzonbie jzonbie) {
        final AppRequest request = get("/");
        final DynamicDefaultAppResponse defaultResponse = dynamicDefault(() -> ok().withBody(objectBody(singletonMap("key", "val"))));
        jzonbie.prime(request, defaultResponse);

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(request);

        assertThat(primedMapping.getResponses().getDefault()).contains(defaultResponse);
    }

    @Test
    void jzonbieCanBePrimedWithAFile(Jzonbie jzonbie) {
        jzonbie.prime(getExamplePrimingFile());

        final List<PrimedMapping> currentPriming = jzonbie.getCurrentPriming();

        assertPrimingFromExamplePrimingFile(currentPriming);
    }

    @Test
    void jzonbieCanBePrimedWithAnInitialPrimingFile() {
        Jzonbie jzonbieWithInitialPrimings = new TestJzonbie(options().withInitialPrimingFile(getExamplePrimingFile()));

        final List<PrimedMapping> currentPriming = jzonbieWithInitialPrimings.getCurrentPriming();

        assertPrimingFromExamplePrimingFile(currentPriming);
    }

    @Test
    void initialPrimingWithAFileIsRemovedOnReset() {
        Jzonbie jzonbieWithInitialPrimings = new TestJzonbie(options().withInitialPrimingFile(getExamplePrimingFile()));

        jzonbieWithInitialPrimings.reset();

        final List<PrimedMapping> currentPriming = jzonbieWithInitialPrimings.getCurrentPriming();

        assertThat(currentPriming).isEmpty();
    }

    @Test
    void jzonbieWithAMissingInitialPrimingFile() {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(
                () -> new TestJzonbie(options().withInitialPrimingFile(new File("missing")))
        ).withCauseInstanceOf(FileNotFoundException.class);
    }

    @Test
    void zombieHeaderNameCanBeSet() {
        final String zombieHeaderName = "jzonbie";
        final Jzonbie jzonbieWithZombieHeaderNameSet = new TestJzonbie(options().withZombieHeaderName(zombieHeaderName));

        final AppRequest request = get("/");
        final AppResponse response = ok().withBody(objectBody(singletonMap("key", "val")));
        jzonbieWithZombieHeaderNameSet.prime(request, response);

        final JzonbieClient apacheJzonbieHttpClient = new ApacheJzonbieHttpClient(
                "http://localhost:" + jzonbieWithZombieHeaderNameSet.getHttpPort(),
                zombieHeaderName
        );

        final List<PrimedMapping> got = apacheJzonbieHttpClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(request);
        assertThat(primedMapping.getResponses().getPrimed()).containsOnly(response);

        jzonbieWithZombieHeaderNameSet.stop();
    }

    @Test
    void verifyThrowsVerificationExceptionIfCallVerificationCriteriaIsFalse(Jzonbie jzonbie) throws Exception {
        final AppRequest request = get("/");
        callJzonbieWithRequest(3, jzonbie, request, ok().withBody(objectBody(singletonMap("key", "val"))), true);

        assertThatThrownBy(() -> jzonbie.verify(request, equalTo(2)))
                .isExactlyInstanceOf(VerificationException.class)
                .hasMessageContaining("3")
                .hasMessageContaining("equal to 2");
    }

    @Test
    void verifyDoesNotThrowExceptionIfCallVerificationCriteriaIsTrue(Jzonbie jzonbie) throws Exception {
        final AppRequest request = get("/");
        callJzonbieWithRequest(2, jzonbie, request, ok().withBody(objectBody(singletonMap("key", "val"))), true);

        jzonbie.verify(request, equalTo(2));
    }

    @Test
    void verifyDoesNotThrowExceptionIfNoVerificationIsPassedAndCallIsMadeOnce(Jzonbie jzonbie) throws Exception {
        final AppRequest request = get("/");
        callJzonbieWithRequest(1, jzonbie, request, ok().withBody(objectBody(singletonMap("key", "val"))), true);

        jzonbie.verify(request);
    }

    @Test
    void getFailedRequestReturnsRequestForWhichThereIsNoPriming(Jzonbie jzonbie) throws Exception {
        client.execute(httpRequest);

        final AppRequest expectedRequest = get("/");

        final List<AppRequest> got = jzonbie.getFailedRequests();

        assertThat(got).hasSize(1);

        assertThat(expectedRequest.matches(got.get(0))).isTrue();
    }

    @Test
    void getFailedRequestReturnsEmptyListIfThereAreNoFailedRequests(Jzonbie jzonbie) {
        final List<AppRequest> got = jzonbie.getFailedRequests();

        assertThat(got).isEmpty();
    }

    @Test
    void stopDoesNotDelayIfNotConfiguredTo() {
        final Jzonbie jzonbie = new TestJzonbie();

        final Stopwatch stopwatch = Stopwatch.createStarted();
        jzonbie.stop();
        stopwatch.stop();

        assertThat(stopwatch.elapsed()).isLessThan(Duration.ofMillis(50));
    }

    @Test
    void waitAfterStopCanBeConfigured() {
        final Duration waitAfterStopFor = Duration.ofSeconds(2);
        final Jzonbie jzonbie = new TestJzonbie(options().withWaitAfterStopping(waitAfterStopFor));

        final Stopwatch stopwatch = Stopwatch.createStarted();
        jzonbie.stop();
        stopwatch.stop();

        assertThat(stopwatch.elapsed()).isGreaterThan(waitAfterStopFor);
    }

    @Test
    void additionalRoutesCanBeAdded() throws IOException {
        final Jzonbie jzonbie = new TestJzonbie(options().withRoutes(JzonbieRoute.get("/ready", ctx -> ctx.getRouteContext().getResponse().ok())));

        try {
            final HttpResponse got = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + "/ready").build());

            assertThat(got.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
        } finally {
            jzonbie.stop();
        }
    }

    @Test
    void additionalRoutesOverridePriming() throws IOException {
        final Jzonbie jzonbie = new TestJzonbie(options().withRoutes(JzonbieRoute.get("/ready", ctx -> ctx.getRouteContext().getResponse().ok())));

        try {
            jzonbie.prime(get("/ready"), internalServerError());

            final HttpResponse got = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + "/ready").build());

            assertThat(got.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
        } finally {
            jzonbie.stop();
        }
    }

    @Test
    void jzonbieReturns404FoundNonPrimedRequest() throws IOException {
        final Jzonbie jzonbie = new TestJzonbie(
                options().withPriming(
                        priming(get("/"), ok())
                )
        );

        try {
            final HttpResponse got1 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + "/").build());

            assertThat(got1.getStatusLine().getStatusCode()).isEqualTo(SC_OK);

            final HttpResponse got2 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + "/").build());

            assertThat(got2.getStatusLine().getStatusCode()).isEqualTo(SC_NOT_FOUND);
        } finally {
            jzonbie.stop();
        }
    }

    @Test
    void jzonbieCanBePrimedWithDefaultResponseDefaultPriming() throws IOException {
        final Jzonbie jzonbie = new TestJzonbie(
                options().withPriming(
                        priming(get("/"), ok()),
                        defaultPriming(get("/default"), staticDefault(ok()))
                )
        );

        try {
            final HttpResponse got1 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + "/default").build());

            assertThat(got1.getStatusLine().getStatusCode()).isEqualTo(SC_OK);

            final HttpResponse got2 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + "/default").build());

            assertThat(got2.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
        } finally {
            jzonbie.stop();
        }
    }

    @Test
    void jzonbieCanBePrimedWithTemplatedResponseDefaultPriming() throws IOException {
        final Jzonbie jzonbie = new TestJzonbie(
                options().withPriming(
                        priming(get("/path"), ok()),
                        defaultPriming(get("/default/templated/path"), staticDefault(ok().templated().withBody(literalBody("{{ request.path }}"))))
                )
        );

        try {
            final String path = "/default/templated/path";
            final HttpResponse got1 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + path).build());

            assertThat(got1.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
            assertThat(EntityUtils.toString(got1.getEntity())).isEqualTo(path);

            final HttpResponse got2 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + path).build());

            assertThat(got2.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
            assertThat(EntityUtils.toString(got2.getEntity())).isEqualTo(path);

            final HttpResponse got3 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + "/path").build());

            assertThat(got3.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
            assertThat(EntityUtils.toString(got3.getEntity())).isEmpty();

        } finally {
            jzonbie.stop();
        }
    }

    @Test
    void jzonbieCanBePrimedWithRequestSessionCountTemplateDefaultPriming() throws IOException {
        final Jzonbie jzonbie = new TestJzonbie(
                options().withPriming(
                        defaultPriming(get("/default/templated/path"), staticDefault(ok().templated().withBody(literalBody("{{ ENDPOINT_REQUEST_COUNT }}"))))
                )
        );

        try {
            final String path = "/default/templated/path";
            final HttpResponse got1 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + path).build());

            assertThat(got1.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
            assertThat(EntityUtils.toString(got1.getEntity())).isEqualTo("1");

            final HttpResponse got2 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + path).build());

            assertThat(got2.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
            assertThat(EntityUtils.toString(got2.getEntity())).isEqualTo("2");

            final HttpResponse got3 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + path).build());

            assertThat(got3.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
            assertThat(EntityUtils.toString(got3.getEntity())).isEqualTo("3");

        } finally {
            jzonbie.stop();
        }
    }

    @Test
    void jzonbieCanBePrimedWithEndpointRequestCountTemplatePriming() throws IOException {
        final Jzonbie jzonbie = new Jzonbie(
                options().withPriming(
                        priming(get("/default/templated/path"), ok().templated().withBody(literalBody("{{ ENDPOINT_REQUEST_COUNT }}1"))),
                        priming(get("/default/templated/path"), ok().templated().withBody(literalBody("{{ ENDPOINT_REQUEST_COUNT }}1"))),
                        priming(get("/default/templated/path"), ok().templated().withBody(literalBody("{{ ENDPOINT_REQUEST_COUNT }}1")))
                )
        );

        try {
            final String path = "/default/templated/path";
            final HttpResponse got1 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + path).build());

            assertThat(got1.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
            assertThat(EntityUtils.toString(got1.getEntity())).isEqualTo("11");

            final HttpResponse got2 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + path).build());

            assertThat(got2.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
            assertThat(EntityUtils.toString(got2.getEntity())).isEqualTo("21");

            final HttpResponse got3 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + path).build());

            assertThat(got3.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
            assertThat(EntityUtils.toString(got3.getEntity())).isEqualTo("31");

        } finally {
            jzonbie.stop();
        }
    }

    @Test
    void jzonbieCanBePrimedWithEndpointRequestPersistentCountTemplatePriming() throws IOException {
        File tempHome = org.assertj.core.util.Files.newTemporaryFolder();
        Jzonbie jzonbie = new Jzonbie(
                options().withHomePath(tempHome.getPath())
                        .withPriming(
                        priming(get("/default/templated/path"), ok().templated().withBody(literalBody("{{ ENDPOINT_REQUEST_PERSISTENT_COUNT }}1"))),
                        priming(get("/default/templated/path"), ok().templated().withBody(literalBody("{{ ENDPOINT_REQUEST_PERSISTENT_COUNT }}1")))
                )
        );

        try {
            final String path = "/default/templated/path";
            final HttpResponse got1 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + path).build());

            assertThat(got1.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
            assertThat(EntityUtils.toString(got1.getEntity())).isEqualTo("11");

            final HttpResponse got2 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + path).build());

            assertThat(got2.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
            assertThat(EntityUtils.toString(got2.getEntity())).isEqualTo("21");

             jzonbie = new Jzonbie(
                    options().withHomePath(tempHome.getPath())
                            .withPriming(
                            priming(get("/default/templated/path"), ok().templated().withBody(literalBody("{{ ENDPOINT_REQUEST_PERSISTENT_COUNT }}1"))),
                            priming(get("/default/templated/path"), ok().templated().withBody(literalBody("{{ ENDPOINT_REQUEST_PERSISTENT_COUNT }}1")))
                            )
            );

            final HttpResponse got3 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + path).build());

            assertThat(got3.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
            assertThat(EntityUtils.toString(got3.getEntity())).isEqualTo("31");

            final HttpResponse got4 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + path).build());

            assertThat(got4.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
            assertThat(EntityUtils.toString(got4.getEntity())).isEqualTo("41");

        } finally {
            jzonbie.stop();
        }
    }

    @Test
    void getHttpsPortThrowsExceptionIfHttpsIsNotConfigured(Jzonbie jzonbie) {
        assertThatThrownBy(jzonbie::getHttpsPort)
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("https");
    }

    @Test
    void jzonbieCallHistoryCapacityCanBeSet() throws IOException {
        final Jzonbie jzonbie = new TestJzonbie(
                options().withCallHistoryCapacity(2)
        );

        callJzonbieWithRequest(4, jzonbie, get("/"), ok().withBody(objectBody(singletonMap("key", "val"))), true);

        assertThat(jzonbie.getHistory()).hasSize(2);
    }

    @Test
    void jzonbieFailedRequestsCapacityCanBeSet() throws IOException {
        final Jzonbie jzonbie = new TestJzonbie(
                options().withFailedRequestsCapacity(2)
        );

        callJzonbieWithRequest(4, jzonbie, get("/"), ok().withBody(objectBody(singletonMap("key", "val"))), false);

        assertThat(jzonbie.getFailedRequests()).hasSize(2);
    }

    @Test
    void jzonbieCanBePrimedWithAnDefaultPrimingFile() {
        Jzonbie jzonbieWithDefaultPrimings = new TestJzonbie(options().withDefaultPrimingFile(getExamplePrimingFile()));

        assertPrimingFromExamplePrimingFile(jzonbieWithDefaultPrimings.getCurrentPriming());
    }

    @Test
    void initialPrimingWithAFileIsNotRemovedOnReset() {
        Jzonbie jzonbieWithDefaultPrimings = new TestJzonbie(options().withDefaultPrimingFile(getExamplePrimingFile()));

        jzonbieWithDefaultPrimings.reset();

        assertPrimingFromExamplePrimingFile(jzonbieWithDefaultPrimings.getCurrentPriming());
    }

    @Test
    void jzonbieWithAMissingDefaultPrimingFile() {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(
                () -> new TestJzonbie(options().withDefaultPrimingFile(new File("missing")))
        ).withCauseInstanceOf(FileNotFoundException.class);
    }

    @Test
    void jzonbieCanAddPrimingThroughAFileAndJavaOptions() {
        Jzonbie jzonbieWithDefaultPrimings = new TestJzonbie(options().withDefaultPrimingFile(getExamplePrimingFile()).withPriming(priming(get("/"), ok())));

        assertThat(jzonbieWithDefaultPrimings.getCurrentPriming()).hasSize(2);
    }

    @Test
    void canConfigureJzonbieHomePath() throws IOException {
        Path rootPath = Files.createTempDirectory("tempDirectory");
        new Jzonbie(options().withHomePath(rootPath.toString()));

        File file = Paths.get(rootPath.toString(), ".jzonbie").toFile();

        assertThat(file.exists()).isTrue();
    }

    @Test
    void jzonbieCanGetAppRequestsCount() throws IOException {
        final Jzonbie jzonbie = new TestJzonbie();

        callJzonbieWithRequest(4, jzonbie, get("/"), ok(), true);

        assertThat(jzonbie.getCount(get("/"))).isEqualTo(4);
    }

    @Test
    void jzonbieCanGetAppRequestsPersistentCount() throws IOException {
        final Jzonbie jzonbie = new TestJzonbie();

        callJzonbieWithRequest(4, jzonbie, get("/"), ok(), true);

        assertThat(jzonbie.getPersistentCount(get("/"))).isEqualTo(4);
    }

    void callJzonbieWithRequest(int times, Jzonbie jzonbie, AppRequest request, AppResponse response, boolean shouldPrime) throws IOException {
        if(shouldPrime) {
            jzonbie.prime(request, staticDefault(response));
        }

        final HttpUriRequest clientRequest = RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + "/").build();
        for(int i = 0; i < times; i++) {
            client.execute(clientRequest);
        }
    }

    private File getExamplePrimingFile() {
        return new File(Objects.requireNonNull(getClass().getClassLoader().getResource("example-priming.json")).getFile());
    }

    private void assertPrimingFromExamplePrimingFile(List<PrimedMapping> got) {
        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest().getPath()).isEqualTo("/path");
        assertThat(primedMapping.getResponses().getDefault()).isNotEmpty();
        assertThat(primedMapping.getResponses().getPrimed()).hasSize(1);
        assertThat(primedMapping.getResponses().getPrimed().get(0).getStatusCode()).isEqualTo(201);
    }
}