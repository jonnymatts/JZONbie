package com.jonnymatts.jzonbie;

import com.google.common.base.Stopwatch;
import com.jonnymatts.jzonbie.junit.JzonbieRule;
import com.jonnymatts.jzonbie.pippo.JzonbieRoute;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.DefaultAppResponse;
import com.jonnymatts.jzonbie.responses.DefaultAppResponse.DynamicDefaultAppResponse;
import com.jonnymatts.jzonbie.verification.VerificationException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;
import static com.jonnymatts.jzonbie.defaults.DefaultResponseDefaultPriming.defaultResponseDefaultPriming;
import static com.jonnymatts.jzonbie.defaults.StandardDefaultPriming.defaultPriming;
import static com.jonnymatts.jzonbie.jackson.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.jackson.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.jackson.body.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.requests.AppRequest.post;
import static com.jonnymatts.jzonbie.responses.AppResponse.internalServerError;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.responses.DefaultAppResponse.DynamicDefaultAppResponse.dynamicDefault;
import static com.jonnymatts.jzonbie.responses.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.equalTo;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

public class JzonbieTest {

    @ClassRule public static JzonbieRule jzonbie = JzonbieRule.jzonbie();
    @Rule public ExpectedException expectedException = ExpectedException.none();

    private HttpUriRequest httpRequest;
    private HttpClient client;

    @Before
    public void setUp() throws Exception {
        httpRequest = RequestBuilder.get("http://localhost:" + jzonbie.getPort() + "/").build();

        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(10);
        connectionManager.setDefaultMaxPerRoute(10);
        client = HttpClientBuilder.create().setConnectionManager(connectionManager).build();
    }

    @After
    public void tearDown() throws Exception {
        jzonbie.reset();
    }

    @Test
    public void jzonbieCanBePrimed() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.prime(
                get("/").build(),
                ok().withBody(objectBody(singletonMap("key", "val"))).build()
        );

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());
        assertThat(primedMapping.getResponses().getEntries()).containsOnly(zombiePriming.getResponse());
    }

    @Test
    public void jzonbieCanBePrimedWithStringBodyContent() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.prime(
                post("/").withBody(stringBody("<jzonbie>message</jzonbie>")).build(),
                ok().withBody(stringBody("<response>message</response>")).build()
        );

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());
        assertThat(primedMapping.getResponses().getEntries()).containsOnly(zombiePriming.getResponse());
    }

    @Test
    public void jzonbieCanBePrimedWithListBodyContent() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.prime(
                post("/").withBody(arrayBody(singletonList("request"))).build(),
                ok().withBody(arrayBody(singletonList("response"))).build()
        );

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());
        assertThat(primedMapping.getResponses().getEntries()).containsOnly(zombiePriming.getResponse());
    }

    @Test
    public void jzonbieCanBePrimedWithJsonStringListBodyContent() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.prime(
                post("/").withBody(stringBody("request")).build(),
                ok().withBody(stringBody("response")).build()
        );

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());
        assertThat(primedMapping.getResponses().getEntries()).containsOnly(zombiePriming.getResponse());
    }

    @Test
    public void jzonbieCanBePrimedForStaticDefault() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.prime( get("/").build(),
                staticDefault(ok().build())
        );

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());
        assertThat(primedMapping.getResponses().getDefault().map(DefaultAppResponse::getResponse)).contains(zombiePriming.getResponse());
    }

    @Test
    public void jzonbieCanBePrimedForDynamicDefault() throws Exception {
        final DynamicDefaultAppResponse defaultResponse = dynamicDefault(() -> ok().withBody(objectBody(singletonMap("key", "val"))).build());
        final ZombiePriming zombiePriming = jzonbie.prime( get("/").build(),
                defaultResponse
        );

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());

        assertThat(primedMapping.getResponses().getDefault()).contains(defaultResponse);
    }

    @Test
    public void jzonbieCanBePrimedWithAFile() throws Exception {
        final File file = new File(getClass().getClassLoader().getResource("example-priming.json").getFile());

        final List<PrimedMapping> got = jzonbie.prime(file);

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest().getPath()).isEqualTo("/path");
        assertThat(primedMapping.getResponses().getDefault()).isNotEmpty();
        assertThat(primedMapping.getResponses().getEntries()).hasSize(1);
        assertThat(primedMapping.getResponses().getEntries().get(0).getStatusCode()).isEqualTo(201);
    }

    // TODO: do this
//    @Test
//    public void zombieHeaderNameCanBeSet() throws Exception {
//        final String zombieHeaderName = "jzonbie";
//        final Jzonbie jzonbieWithZombieHeaderNameSet = new Jzonbie(options().withZombieHeaderName(zombieHeaderName));
//
//        final ZombiePriming zombiePriming = jzonbieWithZombieHeaderNameSet.prime( get("/").build(),
//                ok().withBody(objectBody(singletonMap("key", "val"))).build()
//        );
//
//        final JzonbieClient apacheJzonbieHttpClient = new ApacheJzonbieHttpClient(
//                "http://localhost:" + jzonbieWithZombieHeaderNameSet.getPort(),
//                zombieHeaderName
//        );
//
//        final List<PrimedMapping> got = apacheJzonbieHttpClient.getCurrentPriming();
//
//        assertThat(got).hasSize(1);
//
//        final PrimedMapping primedMapping = got.get(0);
//
//        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());
//        assertThat(primedMapping.getResponses().getEntries()).containsOnly(zombiePriming.getResponse());
//
//        jzonbieWithZombieHeaderNameSet.stop();
//    }

    @Test
    public void verifyThrowsVerificationExceptionIfCallVerificationCriteriaIsFalse() throws Exception {
        final ZombiePriming zombiePriming = callJzonbieWithPrimedRequest(3);

        expectedException.expect(VerificationException.class);
        expectedException.expectMessage("3");
        expectedException.expectMessage("equal to 2");

        jzonbie.verify(zombiePriming.getRequest(), equalTo(2));
    }

    @Test
    public void verifyDoesNotThrowExceptionIfCallVerificationCriteriaIsTrue() throws Exception {
        final ZombiePriming zombiePriming = callJzonbieWithPrimedRequest(2);

        jzonbie.verify(zombiePriming.getRequest(), equalTo(2));
    }

    @Test
    public void verifyDoesNotThrowExceptionIfNoVerificationIsPassedAndCallIsMadeOnce() throws Exception {
        final ZombiePriming zombiePriming = callJzonbieWithPrimedRequest(1);

        jzonbie.verify(zombiePriming.getRequest());
    }

    @Test
    public void getFailedRequestReturnsRequestForWhichThereIsNoPriming() throws Exception {
        client.execute(httpRequest);

        final AppRequest expectedRequest = get("/").build();

        final List<AppRequest> got = jzonbie.getFailedRequests();

        assertThat(got).hasSize(1);

        assertThat(expectedRequest.matches(got.get(0))).isTrue();
    }

    @Test
    public void getFailedRequestReturnsEmptyListIfThereAreNoFailedRequests() throws Exception {
        final List<AppRequest> got = jzonbie.getFailedRequests();

        assertThat(got).isEmpty();
    }

    @Test
    public void stopDoesNotDelayIfNotConfiguredTo() {
        final Jzonbie jzonbie = new Jzonbie(options());

        final Stopwatch stopwatch = Stopwatch.createStarted();
        jzonbie.stop();
        stopwatch.stop();

        assertThat(stopwatch.elapsed()).isLessThan(Duration.ofMillis(50));
    }

    @Test
    public void waitAfterStopCanBeConfigured() {
        final Duration waitAfterStopFor = Duration.ofSeconds(2);
        final Jzonbie jzonbie = new Jzonbie(options().withWaitAfterStopping(waitAfterStopFor));

        final Stopwatch stopwatch = Stopwatch.createStarted();
        jzonbie.stop();
        stopwatch.stop();

        assertThat(stopwatch.elapsed()).isGreaterThan(waitAfterStopFor);
    }

    @Test
    public void additionalRoutesCanBeAdded() throws IOException {
        final Jzonbie jzonbie = new Jzonbie(options().withRoutes(JzonbieRoute.get("/ready", ctx -> ctx.getRouteContext().getResponse().ok())));

        try {
            final HttpResponse got = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getPort() + "/ready").build());

            assertThat(got.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
        } catch(Exception e) {
            throw e;
        } finally {
            jzonbie.stop();
        }
    }

    @Test
    public void additionalRoutesOverridePriming() throws IOException {
        final Jzonbie jzonbie = new Jzonbie(options().withRoutes(JzonbieRoute.get("/ready", ctx -> ctx.getRouteContext().getResponse().ok())));

        try {
            jzonbie.prime(get("/ready").build(), internalServerError().build());

            final HttpResponse got = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getPort() + "/ready").build());

            assertThat(got.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
        } catch(Exception e) {
            throw e;
        } finally {
            jzonbie.stop();
        }
    }

    @Test
    public void jzonbieCanBePrimedWithDefaultPriming() throws IOException {
        final Jzonbie jzonbie = new Jzonbie(
                options().withDefaultPriming(
                        defaultPriming(get("/").build(), ok().build()),
                        defaultResponseDefaultPriming(get("/default").build(), staticDefault(ok().build()))
                )
        );

        try {
            final HttpResponse got1 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getPort() + "/").build());

            assertThat(got1.getStatusLine().getStatusCode()).isEqualTo(SC_OK);

            final HttpResponse got2 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getPort() + "/").build());

            assertThat(got2.getStatusLine().getStatusCode()).isEqualTo(SC_NOT_FOUND);
        } finally {
            jzonbie.stop();
        }
    }

    @Test
    public void jzonbieCanBePrimedWithDefaultResponseDefaultPriming() throws IOException {
        final Jzonbie jzonbie = new Jzonbie(
                options().withDefaultPriming(
                        defaultPriming(get("/").build(), ok().build()),
                        defaultResponseDefaultPriming(get("/default").build(), staticDefault(ok().build()))
                )
        );

        try {
            final HttpResponse got1 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getPort() + "/default").build());

            assertThat(got1.getStatusLine().getStatusCode()).isEqualTo(SC_OK);

            final HttpResponse got2 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getPort() + "/default").build());

            assertThat(got2.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
        } finally {
            jzonbie.stop();
        }
    }

    @Test
    public void jzonbieCanBePrimedWithTemplatedResponseDefaultPriming() throws IOException {
        final Jzonbie jzonbie = new Jzonbie(
                options().withDefaultPriming(
                        defaultPriming(get("/").build(), ok().build()),
                        defaultResponseDefaultPriming(get("/default").build(), staticDefault(ok().build()))
                )
        );

        try {
            final String path = "/templated/path";
            final HttpResponse got1 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getPort() + path).build());

            assertThat(got1.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
            assertThat(EntityUtils.toString(got1.getEntity())).isEqualTo(path);

            final HttpResponse got2 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getPort() + path).build());

            assertThat(got2.getStatusLine().getStatusCode()).isEqualTo(SC_NOT_FOUND);
        } finally {
            jzonbie.stop();
        }
    }

    private ZombiePriming callJzonbieWithPrimedRequest(int times) throws IOException {
        final ZombiePriming zombiePriming = jzonbie.prime(
                get("/").build(),
                staticDefault(ok().withBody(objectBody(singletonMap("key", "val"))).build())
        );

        for(int i = 0; i < times; i++) {
            client.execute(httpRequest);
        }

        return zombiePriming;
    }
}