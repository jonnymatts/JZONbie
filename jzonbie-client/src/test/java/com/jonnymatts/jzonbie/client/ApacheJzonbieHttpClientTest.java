package com.jonnymatts.jzonbie.client;

import com.jonnymatts.jzonbie.JzonbieClient;
import com.jonnymatts.jzonbie.junit.JzonbieRule;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultingQueue;
import com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse;
import com.jonnymatts.jzonbie.util.TestingClient;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import com.jonnymatts.jzonbie.verification.VerificationException;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Consumer;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse.staticDefault;
import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.equalTo;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(Theories.class)
public class ApacheJzonbieHttpClientTest {

    private static final AppRequest REQUEST = get("/").build();
    private static final AppResponse RESPONSE = ok().build();
    private static final StaticDefaultAppResponse DEFAULT_RESPONSE = staticDefault(RESPONSE);
    private static final File FILE = new File(ApacheJzonbieHttpClient.class.getClassLoader().getResource("example-priming.json").getFile());

    @ClassRule public static JzonbieRule jzonbie = JzonbieRule.jzonbie();

    private ZombiePriming zombiePriming;
    private PrimedMapping primedMapping;

    private TestingClient testingClient;

    private ApacheJzonbieHttpClient underTest;
    private ApacheJzonbieHttpClient brokenClient;

    @Before
    public void setUp() {
        zombiePriming = new ZombiePriming(REQUEST, RESPONSE);
        primedMapping = createPrimedMapping(RESPONSE);

        final String zombieBaseUrl = "http://localhost:" + jzonbie.getPort();
        underTest = new ApacheJzonbieHttpClient(zombieBaseUrl);
        brokenClient = new ApacheJzonbieHttpClient("http://broken:8080");
        testingClient = new TestingClient(zombieBaseUrl);
    }

    @After
    public void tearDown() {
        jzonbie.reset();
    }

    @Test
    public void primeZombieReturnsRequestedPriming() {
        final ZombiePriming got = underTest.prime(REQUEST, RESPONSE);

        assertThat(got).isEqualTo(zombiePriming);
    }

    @Test
    public void primeZombieAddsPriming() {
        underTest.prime(REQUEST, RESPONSE);

        assertThat(jzonbie.getCurrentPriming()).containsExactly(primedMapping);
    }

    @Test
    public void primeZombieWithDefaultResponseReturnsRequestedPriming() {
        final ZombiePriming got = underTest.prime(REQUEST, DEFAULT_RESPONSE);

        assertThat(got).isEqualTo(zombiePriming);
    }

    @Test
    public void primeZombieWithDefaultResponseAddsPriming() {
        underTest.prime(REQUEST, DEFAULT_RESPONSE);

        final PrimedMapping primedMapping = createPrimedMapping(DEFAULT_RESPONSE);

        assertThat(jzonbie.getCurrentPriming()).containsExactly(primedMapping);
    }

    @Test
    public void primeZombieWithFileReturnsPrimedMappings() {
        final List<PrimedMapping> got = underTest.prime(FILE);

        assertThat(got).containsExactly(primedMapping);
    }

    @Test
    public void primeZombieWithFileAddsPriming() {
        underTest.prime(FILE);

        assertThat(jzonbie.getCurrentPriming()).containsExactly(primedMapping);
    }

    @Test
    public void getCurrentPrimingReturnsPrimedMappings() {
        underTest.prime(REQUEST, RESPONSE);

        final List<PrimedMapping> got = underTest.getCurrentPriming();

        assertThat(got).containsExactly(primedMapping);
    }

    @Test
    public void getHistoryReturnsCallHistory() {
        underTest.prime(REQUEST, RESPONSE);
        testingClient.execute(REQUEST);

        final List<ZombiePriming> got = underTest.getHistory();

        assertThat(got).hasSize(1);

        got.get(0).getRequest().getHeaders().clear();

        assertThat(got).containsExactly(this.zombiePriming);
    }

    @Test
    public void getFailedRequestsReturnsFailedRequests() {
        testingClient.execute(REQUEST);

        final List<AppRequest> got = underTest.getFailedRequests();

        got.get(0).getHeaders().clear();

        assertThat(got).containsExactly(REQUEST);
    }

    @Test
    public void resetExecutesResetRequest() {
        testingClient.execute(REQUEST);
        underTest.prime(REQUEST, RESPONSE);
        testingClient.execute(REQUEST);

        assertThat(underTest.getFailedRequests()).hasSize(1);
        assertThat(underTest.getHistory()).hasSize(1);

        underTest.reset();

        assertThat(underTest.getFailedRequests()).isEmpty();
        assertThat(underTest.getHistory()).isEmpty();
    }

    @Test
    public void verifyDoesNotThrowExceptionWhenVerificationIsTrue() {
        underTest.prime(REQUEST, RESPONSE);
        underTest.prime(REQUEST, RESPONSE);
        testingClient.execute(REQUEST);
        testingClient.execute(REQUEST);

        underTest.verify(REQUEST, equalTo(2));
    }

    @Test
    public void verifyDoesNotThrowExceptionWhenVerificationIsTrueAndNoCriteriaIsPassedIn() {
        underTest.prime(REQUEST, RESPONSE);
        testingClient.execute(REQUEST);

        underTest.verify(REQUEST);
    }

    @Test
    public void verifyThrowsVerificationExceptionWhenVerificationIsFalse() {
        final InvocationVerificationCriteria criteria = equalTo(2);

        underTest.prime(REQUEST, RESPONSE);
        testingClient.execute(REQUEST);

        assertThatThrownBy(() -> underTest.verify(REQUEST, criteria))
                .isInstanceOf(VerificationException.class)
                .hasMessageContaining("1")
                .hasMessageContaining("equal to 2");
    }

    @Test
    public void verifyThrowsVerificationExceptionWhenVerificationIsFalseAndNoCriteriaIsPassedIn() {
        assertThatThrownBy(() -> underTest.verify(REQUEST))
                .isInstanceOf(VerificationException.class)
                .hasMessageContaining("0")
                .hasMessageContaining("equal to 1");
    }

    @DataPoints("exceptionTests")
    public static ExceptionTestData[] exceptionTestDataPoints = new ExceptionTestData[]{
            new ExceptionTestData("priming", "prime", client -> client.prime(REQUEST, RESPONSE)),
            new ExceptionTestData("default priming", "prime", client -> client.prime(REQUEST, DEFAULT_RESPONSE)),
            new ExceptionTestData("current priming", "current", JzonbieClient::getCurrentPriming),
            new ExceptionTestData("history", "history", JzonbieClient::getHistory),
            new ExceptionTestData("failed requests", "failed", JzonbieClient::getFailedRequests),
            new ExceptionTestData("reset", "reset", JzonbieClient::reset),
            new ExceptionTestData("verify", "count", client -> client.verify(REQUEST)),
    };

    @Theory
    public void clientThrowsExceptionIfHttpClientThrowsException(@FromDataPoints("exceptionTests") ExceptionTestData exceptionTestData) {
        System.out.println("Running client exception test: " + exceptionTestData.name);
        assertThatThrownBy(() -> exceptionTestData.consumer.accept(brokenClient))
                .isInstanceOf(JzonbieClientException.class)
                .hasMessageContaining(exceptionTestData.messageSubstring)
                .hasCauseInstanceOf(UnknownHostException.class);
    }

    private PrimedMapping createPrimedMapping(AppResponse... appResponses) {
        return createPrimedMapping(null, appResponses);
    }

    private PrimedMapping createPrimedMapping(DefaultAppResponse defaultAppResponse, AppResponse... appResponses) {
        final DefaultingQueue defaultingQueue = new DefaultingQueue();
        if(defaultAppResponse != null) {
            defaultingQueue.setDefault(defaultAppResponse);
        }
        defaultingQueue.add(asList(appResponses));
        return new PrimedMapping(REQUEST, defaultingQueue);
    }

    private static class ExceptionTestData {
        private final String name;
        private final String messageSubstring;
        private final Consumer<JzonbieClient> consumer;

        private ExceptionTestData(String name, String messageSubstring, Consumer<JzonbieClient> consumer) {
            this.name = name;
            this.messageSubstring = messageSubstring;
            this.consumer = consumer;
        }
    }
}