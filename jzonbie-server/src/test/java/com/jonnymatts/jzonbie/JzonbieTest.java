package com.jonnymatts.jzonbie;

import com.google.common.base.Stopwatch;
import com.jonnymatts.jzonbie.client.ApacheJzonbieHttpClient;
import com.jonnymatts.jzonbie.client.JzonbieClient;
import com.jonnymatts.jzonbie.junit.JzonbieRule;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.pippo.JzonbieRoute;
import com.jonnymatts.jzonbie.response.DefaultAppResponse;
import com.jonnymatts.jzonbie.response.DefaultAppResponse.DynamicDefaultAppResponse;
import com.jonnymatts.jzonbie.verification.VerificationException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;
import static com.jonnymatts.jzonbie.model.AppRequest.get;
import static com.jonnymatts.jzonbie.model.AppRequest.post;
import static com.jonnymatts.jzonbie.model.AppResponse.internalServerError;
import static com.jonnymatts.jzonbie.model.AppResponse.ok;
import static com.jonnymatts.jzonbie.model.TemplatedAppResponse.templated;
import static com.jonnymatts.jzonbie.model.content.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.response.DefaultAppResponse.DynamicDefaultAppResponse.dynamicDefault;
import static com.jonnymatts.jzonbie.response.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.equalTo;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

public class JzonbieTest {

    @ClassRule public static JzonbieRule jzonbie = JzonbieRule.jzonbie();
    @Rule public ExpectedException expectedException = ExpectedException.none();

    private JzonbieClient jzonbieClient;
    private HttpUriRequest httpRequest;
    private HttpClient client;

    @Before
    public void setUp() throws Exception {
        jzonbieClient = new ApacheJzonbieHttpClient("http://localhost:" + jzonbie.getPort());
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
                ok().withBody(singletonMap("key", "val")).build()
        );

        final List<PrimedMapping> got = jzonbieClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());
    }

    @Test
    public void jzonbieCanBePrimedWithTemplatedResponse() throws Exception {
        final AppResponse response = ok().withBody(singletonMap("key", " {{ request.path }}")).build();
        final ZombiePriming zombiePriming = jzonbie.prime(
                get("/").build(),
                templated(response)
        );

        final List<PrimedMapping> got = jzonbieClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(response);
    }

    @Test
    public void jzonbieCanBePrimedWithStringBodyContent() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.prime(
                post("/").withBody("<jzonbie>message</jzonbie>").build(),
                ok().withBody("<response>message</response>").build()
        );

        final List<PrimedMapping> got = jzonbieClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());
    }

    @Test
    public void jzonbieCanBePrimedWithListBodyContent() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.prime(
                post("/").withBody(singletonList("request")).build(),
                ok().withBody(singletonList("response")).build()
        );

        final List<PrimedMapping> got = jzonbieClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());
    }

    @Test
    public void jzonbieCanBePrimedWithJsonStringListBodyContent() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.prime(
                post("/").withBody(stringBody("request")).build(),
                ok().withBody(stringBody("response")).build()
        );

        final List<PrimedMapping> got = jzonbieClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());
    }


    @Test
    public void jzonbieCanBePrimedWithNumberBodyContent() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.prime(
                post("/").withBody(1).build(),
                ok().withBody(2).build()
        );

        final List<PrimedMapping> got = jzonbieClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());
    }

    @Test
    public void jzonbieCanBePrimedForStaticDefault() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.prime( get("/").build(),
                staticDefault(ok().withBody(singletonMap("key", "val")).build())
        );

        final List<PrimedMapping> got = jzonbieClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getDefault().map(DefaultAppResponse::getResponse)).contains(zombiePriming.getAppResponse());
    }

    @Test
    public void jzonbieCanBePrimedForStaticDefaultTemplatedResponse() throws Exception {
        final AppResponse response = ok().withBody(singletonMap("key", "{{ request.path }}")).build();
        final ZombiePriming zombiePriming = jzonbie.prime( get("/").build(),
                staticDefault(templated(response))
        );

        final List<PrimedMapping> got = jzonbieClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getDefault().map(DefaultAppResponse::getResponse)).contains(response);
    }

    @Test
    public void jzonbieCanBePrimedForDynamicDefault() throws Exception {
        final DynamicDefaultAppResponse defaultResponse = dynamicDefault(() -> ok().withBody(singletonMap("key", "val")).build());
        final ZombiePriming zombiePriming = jzonbie.prime( get("/").build(),
                defaultResponse
        );

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());

        assertThat(primedMapping.getAppResponses().getDefault()).contains(defaultResponse);
    }



    @Test
    public void jzonbieCanBePrimedForDynamicDefaultTemplatedResponse() throws Exception {
        final TemplatedAppResponse response = templated(ok().withBody(singletonMap("key", "{{ request.path }}")).build());
        final DynamicDefaultAppResponse defaultResponse = dynamicDefault(() -> response);
        final ZombiePriming zombiePriming = jzonbie.prime( get("/").build(),
                defaultResponse
        );

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());

        assertThat(primedMapping.getAppResponses().getDefault().map(DefaultAppResponse::getResponse).get()).isInstanceOf(TemplatedAppResponse.class);
        assertThat(primedMapping.getAppResponses().getDefault().map(DefaultAppResponse::getResponse)).contains(response);
    }

    @Test
    public void jzonbieCanBePrimedWithAFile() throws Exception {
        final File file = new File(getClass().getClassLoader().getResource("example-priming.json").getFile());

        final List<PrimedMapping> got = jzonbie.prime(file);

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest().getPath()).isEqualTo("/path");
        assertThat(primedMapping.getAppResponses().getDefault()).isNotEmpty();
        assertThat(primedMapping.getAppResponses().getEntries()).hasSize(1);
        assertThat(primedMapping.getAppResponses().getEntries().get(0).getStatusCode()).isEqualTo(201);
    }

    @Test
    public void zombieHeaderNameCanBeSet() throws Exception {
        final String zombieHeaderName = "jzonbie";
        final Jzonbie jzonbieWithZombieHeaderNameSet = new Jzonbie(options().withZombieHeaderName(zombieHeaderName));

        final ZombiePriming zombiePriming = jzonbieWithZombieHeaderNameSet.prime( get("/").build(),
                ok().withBody(singletonMap("key", "val")).build()
        );

        final JzonbieClient apacheJzonbieHttpClient = new ApacheJzonbieHttpClient(
                "http://localhost:" + jzonbieWithZombieHeaderNameSet.getPort(),
                zombieHeaderName
        );

        final List<PrimedMapping> got = apacheJzonbieHttpClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());

        jzonbieWithZombieHeaderNameSet.stop();
    }

    @Test
    public void verifyThrowsVerificationExceptionIfCallVerificationCriteriaIsFalse() throws Exception {
        final ZombiePriming zombiePriming = callJzonbieWithPrimedRequest(3);

        expectedException.expect(VerificationException.class);
        expectedException.expectMessage("3");
        expectedException.expectMessage("equal to 2");

        jzonbie.verify(zombiePriming.getAppRequest(), equalTo(2));
    }

    @Test
    public void verifyDoesNotThrowExceptionIfCallVerificationCriteriaIsTrue() throws Exception {
        final ZombiePriming zombiePriming = callJzonbieWithPrimedRequest(2);

        jzonbie.verify(zombiePriming.getAppRequest(), equalTo(2));
    }

    @Test
    public void verifyDoesNotThrowExceptionIfNoVerificationIsPassedAndCallIsMadeOnce() throws Exception {
        final ZombiePriming zombiePriming = callJzonbieWithPrimedRequest(1);

        jzonbie.verify(zombiePriming.getAppRequest());
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

        final HttpResponse got = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getPort() + "/ready").build());

        assertThat(got.getStatusLine().getStatusCode()).isEqualTo(SC_OK);

        jzonbie.stop();
    }

    @Test
    public void additionalRoutesOverridePriming() throws IOException {
        final Jzonbie jzonbie = new Jzonbie(options().withRoutes(JzonbieRoute.get("/ready", ctx -> ctx.getRouteContext().getResponse().ok())));
        jzonbie.prime(get("/ready").build(), internalServerError().build());

        final HttpResponse got = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getPort() + "/ready").build());

        assertThat(got.getStatusLine().getStatusCode()).isEqualTo(SC_OK);

        jzonbie.stop();
    }

    private ZombiePriming callJzonbieWithPrimedRequest(int times) throws IOException {
        final ZombiePriming zombiePriming = jzonbie.prime(
                get("/").build(),
                staticDefault(ok().withBody(singletonMap("key", "val")).build())
        );

        for(int i = 0; i < times; i++) {
            client.execute(httpRequest);
        }

        return zombiePriming;
    }
}