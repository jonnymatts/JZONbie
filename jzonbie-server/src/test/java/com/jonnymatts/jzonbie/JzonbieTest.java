package com.jonnymatts.jzonbie;

import com.jonnymatts.jzonbie.client.ApacheJzonbieHttpClient;
import com.jonnymatts.jzonbie.client.JzonbieClient;
import com.jonnymatts.jzonbie.junit.JzonbieRule;
import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.response.DefaultAppResponse;
import com.jonnymatts.jzonbie.response.DefaultAppResponse.DynamicDefaultAppResponse;
import com.jonnymatts.jzonbie.verification.VerificationException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;
import static com.jonnymatts.jzonbie.junit.JzonbieRule.*;
import static com.jonnymatts.jzonbie.model.content.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.response.DefaultAppResponse.DynamicDefaultAppResponse.dynamicDefault;
import static com.jonnymatts.jzonbie.response.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.equalTo;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class JzonbieTest {

    @ClassRule public static JzonbieRule jzonbieRule = JzonbieRule.jzonbie();
    @Rule public ExpectedException expectedException = ExpectedException.none();

    private JzonbieClient jzonbieClient;
    private HttpUriRequest httpRequest;
    private HttpClient client;

    @Before
    public void setUp() throws Exception {
        jzonbieClient = new ApacheJzonbieHttpClient("http://localhost:" + port());
        httpRequest = RequestBuilder.get("http://localhost:" + port() + "/").build();

        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(10);
        connectionManager.setDefaultMaxPerRoute(10);
        client = HttpClientBuilder.create().setConnectionManager(connectionManager).build();
    }

    @After
    public void tearDown() throws Exception {
        reset();
    }

    @Test
    public void jzonbieCanBePrimed() throws Exception {
        final ZombiePriming zombiePriming = prime(
                AppRequest.builder("GET", "/").build(),
                AppResponse.builder(200).withBody(singletonMap("key", "val")).build()
        );

        final List<PrimedMapping> got = jzonbieClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());
    }

    @Test
    public void jzonbieCanBePrimedWithStringBodyContent() throws Exception {
        final ZombiePriming zombiePriming = prime(
                AppRequest.builder("POST", "/").withBody("<jzonbie>message</jzonbie>").build(),
                AppResponse.builder(200).withBody("<response>message</response>").build()
        );

        final List<PrimedMapping> got = jzonbieClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());
    }

    @Test
    public void jzonbieCanBePrimedWithListBodyContent() throws Exception {
        final ZombiePriming zombiePriming = prime(
                AppRequest.builder("POST", "/").withBody(singletonList("request")).build(),
                AppResponse.builder(200).withBody(singletonList("response")).build()
        );

        final List<PrimedMapping> got = jzonbieClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());
    }

    @Test
    public void jzonbieCanBePrimedWithJsonStringListBodyContent() throws Exception {
        final ZombiePriming zombiePriming = prime(
                AppRequest.builder("POST", "/").withBody(stringBody("request")).build(),
                AppResponse.builder(200).withBody(stringBody("response")).build()
        );

        final List<PrimedMapping> got = jzonbieClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());
    }


    @Test
    public void jzonbieCanBePrimedWithNumberBodyContent() throws Exception {
        final ZombiePriming zombiePriming = prime(
                AppRequest.builder("POST", "/").withBody(1).build(),
                AppResponse.builder(200).withBody(2).build()
        );

        final List<PrimedMapping> got = jzonbieClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());
    }

    @Test
    public void jzonbieCanBePrimedForStaticDefault() throws Exception {
        final ZombiePriming zombiePriming = prime(
                AppRequest.builder("GET", "/").build(),
                staticDefault(AppResponse.builder(200).withBody(singletonMap("key", "val")).build())
        );

        final List<PrimedMapping> got = jzonbieClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getDefault().map(DefaultAppResponse::getResponse)).contains(zombiePriming.getAppResponse());
    }

    @Test
    public void jzonbieCanBePrimedForDynamicDefault() throws Exception {
        final DynamicDefaultAppResponse defaultResponse = dynamicDefault(() -> AppResponse.builder(200).withBody(singletonMap("key", "val")).build());
        final ZombiePriming zombiePriming = prime(
                AppRequest.builder("GET", "/").build(),
                defaultResponse
        );

        final List<PrimedMapping> got = currentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());

        assertThat(primedMapping.getAppResponses().getDefault()).contains(defaultResponse);
    }

    @Test
    public void jzonbieCanBePrimedWithAFile() throws Exception {
        final File file = new File(getClass().getClassLoader().getResource("example-priming.json").getFile());

        final List<PrimedMapping> got = prime(file);

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

        final ZombiePriming zombiePriming = jzonbieWithZombieHeaderNameSet.primeZombie(
                AppRequest.builder("GET", "/").build(),
                AppResponse.builder(200).withBody(singletonMap("key", "val")).build()
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

        verify(zombiePriming.getAppRequest(), equalTo(2));
    }

    @Test
    public void verifyDoesNotThrowExceptionIfCallVerificationCriteriaIsTrue() throws Exception {
        final ZombiePriming zombiePriming = callJzonbieWithPrimedRequest(2);

        verify(zombiePriming.getAppRequest(), equalTo(2));
    }

    @Test
    public void verifyDoesNotThrowExceptionIfNoVerificationIsPassedAndCallIsMadeOnce() throws Exception {
        final ZombiePriming zombiePriming = callJzonbieWithPrimedRequest(1);

        verify(zombiePriming.getAppRequest());
    }

    private ZombiePriming callJzonbieWithPrimedRequest(int times) throws IOException {
        final ZombiePriming zombiePriming = prime(
                AppRequest.builder("GET", "/").build(),
                staticDefault(AppResponse.builder(200).withBody(singletonMap("key", "val")).build())
        );

        for(int i = 0; i < times; i++) {
            client.execute(httpRequest);
        }

        return zombiePriming;
    }
}