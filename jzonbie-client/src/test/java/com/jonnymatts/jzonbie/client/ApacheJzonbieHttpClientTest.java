package com.jonnymatts.jzonbie.client;

import com.jonnymatts.jzonbie.Jzonbie;
import com.jonnymatts.jzonbie.JzonbieClient;
import com.jonnymatts.jzonbie.history.Exchange;
import com.jonnymatts.jzonbie.junit.JzonbieExtension;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultingQueue;
import com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse;
import com.jonnymatts.jzonbie.util.TestingClient;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import com.jonnymatts.jzonbie.verification.VerificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sun.security.x509.X509CertImpl;

import java.io.File;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.io.Resources.getResource;
import static com.jonnymatts.jzonbie.HttpsOptions.httpsOptions;
import static com.jonnymatts.jzonbie.JzonbieOptions.options;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse.staticDefault;
import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.equalTo;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(JzonbieExtension.class)
class ApacheJzonbieHttpClientTest {

    private static final AppRequest REQUEST = get("/");
    private static final AppResponse RESPONSE = ok();
    private static final StaticDefaultAppResponse DEFAULT_RESPONSE = staticDefault(RESPONSE);
    private static final File FILE = new File(ApacheJzonbieHttpClient.class.getClassLoader().getResource("example-priming.json").getFile());

    private Exchange exchange;
    private PrimedMapping primedMapping;

    private TestingClient testingClient;

    private ApacheJzonbieHttpClient underTest;
    private ApacheJzonbieHttpClient brokenClient;

    @BeforeEach
    void setUp(Jzonbie jzonbie) {
        exchange = new Exchange(REQUEST, RESPONSE);
        primedMapping = createPrimedMapping(RESPONSE);

        final String zombieBaseUrl = "http://localhost:" + jzonbie.getHttpPort();
        underTest = new ApacheJzonbieHttpClient(zombieBaseUrl);
        brokenClient = new ApacheJzonbieHttpClient("http://broken:8080");
        testingClient = new TestingClient(zombieBaseUrl);
    }

    @Test
    void primeZombieAddsPriming(Jzonbie jzonbie) {
        underTest.prime(REQUEST, RESPONSE);

        assertThat(jzonbie.getCurrentPriming()).containsExactly(primedMapping);
    }

    @Test
    void primeZombieWithDefaultResponseAddsPriming(Jzonbie jzonbie) {
        underTest.prime(REQUEST, DEFAULT_RESPONSE);

        final PrimedMapping primedMapping = createPrimedMapping(DEFAULT_RESPONSE);

        assertThat(jzonbie.getCurrentPriming()).containsExactly(primedMapping);
    }

    @Test
    void primeZombieWithFileAddsPriming(Jzonbie jzonbie) {
        underTest.prime(FILE);

        assertThat(jzonbie.getCurrentPriming()).containsExactly(primedMapping);
    }

    @Test
    void getCurrentPrimingReturnsPrimedMappings() {
        underTest.prime(REQUEST, RESPONSE);

        final List<PrimedMapping> got = underTest.getCurrentPriming();

        assertThat(got).containsExactly(primedMapping);
    }

    @Test
    void getHistoryReturnsCallHistory() {
        underTest.prime(REQUEST, RESPONSE);
        testingClient.execute(REQUEST);

        final List<Exchange> got = underTest.getHistory();

        assertThat(got).hasSize(1);

        got.get(0).getRequest().getHeaders().clear();

        assertThat(got).containsExactly(exchange);
    }

    @Test
    void getFailedRequestsReturnsFailedRequests() {
        testingClient.execute(REQUEST);

        final List<AppRequest> got = underTest.getFailedRequests();

        got.get(0).getHeaders().clear();

        assertThat(got).containsExactly(REQUEST);
    }

    @Test
    void resetExecutesResetRequest() {
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
    void verifyDoesNotThrowExceptionWhenVerificationIsTrue() {
        underTest.prime(REQUEST, RESPONSE);
        underTest.prime(REQUEST, RESPONSE);
        testingClient.execute(REQUEST);
        testingClient.execute(REQUEST);

        underTest.verify(REQUEST, equalTo(2));
    }

    @Test
    void verifyDoesNotThrowExceptionWhenVerificationIsTrueAndNoCriteriaIsPassedIn() {
        underTest.prime(REQUEST, RESPONSE);
        testingClient.execute(REQUEST);

        underTest.verify(REQUEST);
    }

    @Test
    void verifyThrowsVerificationExceptionWhenVerificationIsFalse() {
        final InvocationVerificationCriteria criteria = equalTo(2);

        underTest.prime(REQUEST, RESPONSE);
        testingClient.execute(REQUEST);

        assertThatThrownBy(() -> underTest.verify(REQUEST, criteria))
                .isInstanceOf(VerificationException.class)
                .hasMessageContaining("1")
                .hasMessageContaining("equal to 2");
    }

    @Test
    void verifyThrowsVerificationExceptionWhenVerificationIsFalseAndNoCriteriaIsPassedIn() {
        assertThatThrownBy(() -> underTest.verify(REQUEST))
                .isInstanceOf(VerificationException.class)
                .hasMessageContaining("0")
                .hasMessageContaining("equal to 1");
    }

    @Test
    void getTruststoreThrowsExceptionIfServerIsNotServingHttps() {
        assertThatThrownBy(() -> underTest.getTruststore())
                .isInstanceOf(JzonbieClientException.class)
                .hasMessageContaining("Failed to obtain truststore");
    }

    @Test
    void getTruststoreThrowsExceptionIfKeystoreIsPassedIn() {
        new Jzonbie(options().withHttps(httpsOptions().withKeystoreLocation(getResource("test.jks").toString()).withKeystorePassword("jzonbie")));

        assertThatThrownBy(() -> underTest.getTruststore())
                .isInstanceOf(JzonbieClientException.class)
                .hasMessageContaining("Failed to obtain truststore");
    }

    @Test
    void getTruststoreReturnsKeystoreIfDefaultHttpsConfigurationIsEnabled() throws Exception {
        final Jzonbie httpsJzonbie = new Jzonbie(options().withHttps(httpsOptions()));

        final KeyStore truststore = httpsJzonbie.getTruststore();

        assertThat(new X509CertImpl(truststore.getCertificate("jzonbie").getEncoded()).getSubjectDN().getName()).isEqualTo("CN=localhost");
    }

    static Stream<ExceptionTestData> exceptionTestDataPoints() {
        return Stream.of(
            new ExceptionTestData("priming", "prime", client -> client.prime(REQUEST, RESPONSE)),
            new ExceptionTestData("default priming", "prime", client -> client.prime(REQUEST, DEFAULT_RESPONSE)),
            new ExceptionTestData("current priming", "current", JzonbieClient::getCurrentPriming),
            new ExceptionTestData("history", "history", JzonbieClient::getHistory),
            new ExceptionTestData("failed requests", "failed", JzonbieClient::getFailedRequests),
            new ExceptionTestData("reset", "reset", JzonbieClient::reset),
            new ExceptionTestData("verify", "count", client -> client.verify(REQUEST)),
            new ExceptionTestData("truststore", "truststore", JzonbieClient::getTruststore)
        );
    }

    @ParameterizedTest
    @MethodSource("exceptionTestDataPoints")
    void clientThrowsExceptionIfHttpClientThrowsException(ExceptionTestData exceptionTestData) {
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