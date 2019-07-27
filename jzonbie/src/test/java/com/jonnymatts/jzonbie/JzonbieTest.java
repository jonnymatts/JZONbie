package com.jonnymatts.jzonbie;

import com.google.common.base.Stopwatch;
import com.jonnymatts.jzonbie.client.ApacheJzonbieHttpClient;
import com.jonnymatts.jzonbie.junit.JzonbieExtension;
import com.jonnymatts.jzonbie.pippo.JzonbieRoute;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.requests.AppRequest;
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
import java.io.IOException;
import java.time.Duration;
import java.util.List;

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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(JzonbieExtension.class)
class JzonbieTest {

    private HttpUriRequest httpRequest;
    private HttpClient client;

    @BeforeEach
    void setUp() throws Exception {
        httpRequest = RequestBuilder.get("http://localhost:" + JzonbieExtension.getJzonbie().getHttpPort() + "/").build();

        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(10);
        connectionManager.setDefaultMaxPerRoute(10);
        client = HttpClientBuilder.create().setConnectionManager(connectionManager).build();
    }

    @Test
    void jzonbieCanBePrimed(Jzonbie jzonbie) throws Exception {
        final ZombiePriming zombiePriming = jzonbie.prime(
                get("/"),
                ok().withBody(objectBody(singletonMap("key", "val")))
        );

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());
        assertThat(primedMapping.getResponses().getPrimed()).containsOnly(zombiePriming.getResponse());
    }

    @Test
    void jzonbieCanBePrimedWithStringBodyContent(Jzonbie jzonbie) throws Exception {
        final ZombiePriming zombiePriming = jzonbie.prime(
                post("/").withBody(stringBody("<jzonbie>message</jzonbie>")),
                ok().withBody(stringBody("<response>message</response>"))
        );

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());
        assertThat(primedMapping.getResponses().getPrimed()).containsOnly(zombiePriming.getResponse());
    }

    @Test
    void jzonbieCanBePrimedWithListBodyContent(Jzonbie jzonbie) throws Exception {
        final ZombiePriming zombiePriming = jzonbie.prime(
                post("/").withBody(arrayBody(singletonList("request"))),
                ok().withBody(arrayBody(singletonList("response")))
        );

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());
        assertThat(primedMapping.getResponses().getPrimed()).containsOnly(zombiePriming.getResponse());
    }

    @Test
    void jzonbieCanBePrimedWithJsonStringListBodyContent(Jzonbie jzonbie) throws Exception {
        final ZombiePriming zombiePriming = jzonbie.prime(
                post("/").withBody(stringBody("request")),
                ok().withBody(stringBody("response"))
        );

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());
        assertThat(primedMapping.getResponses().getPrimed()).containsOnly(zombiePriming.getResponse());
    }

    @Test
    void jzonbieCanBePrimedForStaticDefault(Jzonbie jzonbie) throws Exception {
        final ZombiePriming zombiePriming = jzonbie.prime( get("/"),
                staticDefault(ok())
        );

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());
        assertThat(primedMapping.getResponses().getDefault().map(DefaultAppResponse::getResponse)).contains(zombiePriming.getResponse());
    }

    @Test
    void jzonbieCanBePrimedForDynamicDefault(Jzonbie jzonbie) throws Exception {
        final DynamicDefaultAppResponse defaultResponse = dynamicDefault(() -> ok().withBody(objectBody(singletonMap("key", "val"))));
        final ZombiePriming zombiePriming = jzonbie.prime( get("/"),
                defaultResponse
        );

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());

        assertThat(primedMapping.getResponses().getDefault()).contains(defaultResponse);
    }

    @Test
    void jzonbieCanBePrimedWithAFile(Jzonbie jzonbie) throws Exception {
        final File file = new File(getClass().getClassLoader().getResource("example-priming.json").getFile());

        final List<PrimedMapping> got = jzonbie.prime(file);

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest().getPath()).isEqualTo("/path");
        assertThat(primedMapping.getResponses().getDefault()).isNotEmpty();
        assertThat(primedMapping.getResponses().getPrimed()).hasSize(1);
        assertThat(primedMapping.getResponses().getPrimed().get(0).getStatusCode()).isEqualTo(201);
    }

    @Test
    void zombieHeaderNameCanBeSet() throws Exception {
        final String zombieHeaderName = "jzonbie";
        final Jzonbie jzonbieWithZombieHeaderNameSet = new Jzonbie(options().withZombieHeaderName(zombieHeaderName));

        final ZombiePriming zombiePriming = jzonbieWithZombieHeaderNameSet.prime( get("/"),
                ok().withBody(objectBody(singletonMap("key", "val")))
        );

        final JzonbieClient apacheJzonbieHttpClient = new ApacheJzonbieHttpClient(
                "http://localhost:" + jzonbieWithZombieHeaderNameSet.getHttpPort(),
                zombieHeaderName
        );

        final List<PrimedMapping> got = apacheJzonbieHttpClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());
        assertThat(primedMapping.getResponses().getPrimed()).containsOnly(zombiePriming.getResponse());

        jzonbieWithZombieHeaderNameSet.stop();
    }

    @Test
    void verifyThrowsVerificationExceptionIfCallVerificationCriteriaIsFalse(Jzonbie jzonbie) throws Exception {
        final ZombiePriming zombiePriming = callJzonbieWithPrimedRequest(3, jzonbie);

        assertThatThrownBy(() -> jzonbie.verify(zombiePriming.getRequest(), equalTo(2)))
                .isExactlyInstanceOf(VerificationException.class)
                .hasMessageContaining("3")
                .hasMessageContaining("equal to 2");
    }

    @Test
    void verifyDoesNotThrowExceptionIfCallVerificationCriteriaIsTrue(Jzonbie jzonbie) throws Exception {
        final ZombiePriming zombiePriming = callJzonbieWithPrimedRequest(2, jzonbie);

        jzonbie.verify(zombiePriming.getRequest(), equalTo(2));
    }

    @Test
    void verifyDoesNotThrowExceptionIfNoVerificationIsPassedAndCallIsMadeOnce(Jzonbie jzonbie) throws Exception {
        final ZombiePriming zombiePriming = callJzonbieWithPrimedRequest(1, jzonbie);

        jzonbie.verify(zombiePriming.getRequest());
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
    void getFailedRequestReturnsEmptyListIfThereAreNoFailedRequests(Jzonbie jzonbie) throws Exception {
        final List<AppRequest> got = jzonbie.getFailedRequests();

        assertThat(got).isEmpty();
    }

    @Test
    void stopDoesNotDelayIfNotConfiguredTo() {
        final Jzonbie jzonbie = new Jzonbie(options());

        final Stopwatch stopwatch = Stopwatch.createStarted();
        jzonbie.stop();
        stopwatch.stop();

        assertThat(stopwatch.elapsed()).isLessThan(Duration.ofMillis(50));
    }

    @Test
    void waitAfterStopCanBeConfigured() {
        final Duration waitAfterStopFor = Duration.ofSeconds(2);
        final Jzonbie jzonbie = new Jzonbie(options().withWaitAfterStopping(waitAfterStopFor));

        final Stopwatch stopwatch = Stopwatch.createStarted();
        jzonbie.stop();
        stopwatch.stop();

        assertThat(stopwatch.elapsed()).isGreaterThan(waitAfterStopFor);
    }

    @Test
    void additionalRoutesCanBeAdded() throws IOException {
        final Jzonbie jzonbie = new Jzonbie(options().withRoutes(JzonbieRoute.get("/ready", ctx -> ctx.getRouteContext().getResponse().ok())));

        try {
            final HttpResponse got = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + "/ready").build());

            assertThat(got.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
        } catch(Exception e) {
            throw e;
        } finally {
            jzonbie.stop();
        }
    }

    @Test
    void additionalRoutesOverridePriming() throws IOException {
        final Jzonbie jzonbie = new Jzonbie(options().withRoutes(JzonbieRoute.get("/ready", ctx -> ctx.getRouteContext().getResponse().ok())));

        try {
            jzonbie.prime(get("/ready"), internalServerError());

            final HttpResponse got = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + "/ready").build());

            assertThat(got.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
        } catch(Exception e) {
            throw e;
        } finally {
            jzonbie.stop();
        }
    }

    @Test
    void jzonbieCanBePrimedWithDefaultPriming() throws IOException {
        final Jzonbie jzonbie = new Jzonbie(
                options().withPriming(
                        priming(get("/"), ok()),
                        defaultPriming(get("/default"), staticDefault(ok()))
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
        final Jzonbie jzonbie = new Jzonbie(
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
        final Jzonbie jzonbie = new Jzonbie(
                options().withPriming(
                        priming(get("/templated/path"), ok().templated().withBody(literalBody("{{ request.path }}"))),
                        defaultPriming(get("/default"), staticDefault(ok()))
                )
        );

        try {
            final String path = "/templated/path";
            final HttpResponse got1 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + path).build());

            assertThat(got1.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
            assertThat(EntityUtils.toString(got1.getEntity())).isEqualTo(path);

            final HttpResponse got2 = client.execute(RequestBuilder.get("http://localhost:" + jzonbie.getHttpPort() + path).build());

            assertThat(got2.getStatusLine().getStatusCode()).isEqualTo(SC_NOT_FOUND);
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

    ZombiePriming callJzonbieWithPrimedRequest(int times, Jzonbie jzonbie) throws IOException {
        final ZombiePriming zombiePriming = jzonbie.prime(
                get("/"),
                staticDefault(ok().withBody(objectBody(singletonMap("key", "val"))))
        );

        for(int i = 0; i < times; i++) {
            client.execute(httpRequest);
        }

        return zombiePriming;
    }
}