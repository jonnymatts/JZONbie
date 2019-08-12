package com.jonnymatts.jzonbie.client;

import com.jonnymatts.jzonbie.JzonbieClient;
import com.jonnymatts.jzonbie.history.Exchange;
import com.jonnymatts.jzonbie.jackson.Deserializer;
import com.jonnymatts.jzonbie.jackson.JzonbieObjectMapper;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DynamicDefaultAppResponse;
import com.jonnymatts.jzonbie.verification.CountResult;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import com.jonnymatts.jzonbie.verification.VerificationException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.util.List;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Base64.getDecoder;
import static java.util.function.Function.identity;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * Class to communicate with a Jzonbie over HTTP.
 * <p>
 * All methods defined on the {@link JzonbieClient} interface are implemented.
 * However, the {@code ApacheJzonbieHttpClient} is unable to send or receive
 * {@link DynamicDefaultAppResponse}s.
 * <p>
 * {@code
 * final JzonbieClient jzonbie = new ApacheJzonbieHttpClient("http://jzonbie");
 *
 * jzonbie.prime(get("/"), ok());
 * }
 */
public class ApacheJzonbieHttpClient implements JzonbieClient {

    private final ApacheJzonbieRequestFactory apacheJzonbieRequestFactory;
    private final CloseableHttpClient httpClient;
    private final Deserializer deserializer;

    /**
     * Creates a new client, communicating with a Jzonbie at the base URL.
     *
     * @param zombieBaseUrl base URL of the Jzonbie
     */
    public ApacheJzonbieHttpClient(String zombieBaseUrl) {
        final JzonbieObjectMapper objectMapper = new JzonbieObjectMapper();

        this.apacheJzonbieRequestFactory = new ApacheJzonbieRequestFactory(zombieBaseUrl, objectMapper);
        this.httpClient = HttpClientBuilder.create().build();
        this.deserializer = new Deserializer();
    }

    /**
     * Creates a new client, communicating with a Jzonbie at the base URL.
     * The zombie header is the name of the header that is used to drive
     * Jzonbie functions over HTTP.
     *
     * @param zombieBaseUrl base URL of the Jzonbie
     * @param zombieHeaderName zombie header of the Jzonbie
     */
    public ApacheJzonbieHttpClient(String zombieBaseUrl,
                                   String zombieHeaderName) {
        final JzonbieObjectMapper objectMapper = new JzonbieObjectMapper();

        this.apacheJzonbieRequestFactory = new ApacheJzonbieRequestFactory(zombieBaseUrl, zombieHeaderName, objectMapper);
        this.httpClient = HttpClientBuilder.create().build();
        this.deserializer = new Deserializer(objectMapper);
    }

    /**
     * Creates a new client, communicating with a Jzonbie at the base URL.
     * Passing in a deserializer allows the client to serialize other classes.
     * <p>
     * <pre>
     * {@code
     * final CloseableHttpClient client = HttpClients.createDefault();
     * final ApacheJzonbieRequestFactory requestFactory = new ApacheJzonbieRequestFactory("http://jzonbie");
     * final Deserializer deserializer = new Deserializer(new CustomObjectMapper());
     * final JzonbieClient jzonbie = new ApacheJzonbieHttpClient(client, requestFactory, deserializer);
     *
     * jzonbie.prime(get("/"), ok());
     * }
     * </pre>
     *
     * @param httpClient base client
     * @param apacheJzonbieRequestFactory request factory
     * @param deserializer body deserializer
     */
    public ApacheJzonbieHttpClient(CloseableHttpClient httpClient,
                                   ApacheJzonbieRequestFactory apacheJzonbieRequestFactory,
                                   Deserializer deserializer) {
        this.httpClient = httpClient;
        this.apacheJzonbieRequestFactory = apacheJzonbieRequestFactory;
        this.deserializer = deserializer;
    }

    @Override
    public void prime(AppRequest request, AppResponse response) {
        final HttpUriRequest primeZombieRequest = apacheJzonbieRequestFactory.createPrimeZombieRequest(request, response);
        execute(
                primeZombieRequest,
                httpResponse -> deserializer.deserialize(getHttpResponseBody(httpResponse), ZombiePriming.class),
                format("Failed to prime. %s, %s", request, response)
        );
    }

    @Override
    public void prime(File file) {
        final HttpUriRequest primeZombieRequest = apacheJzonbieRequestFactory.createPrimeZombieWithFileRequest(file);
        execute(
                primeZombieRequest, httpResponse -> deserializer.deserializeCollection(getHttpResponseBody(httpResponse), PrimedMapping.class),
                format("Failed to prime with file %s.", file.getAbsolutePath())
        );
    }

    /**
     * Prime this Jzonbie to return response when an incoming request matches the input request
     * and there are no responses primed.
     * <p>
     * This is a permanent priming. It can only be removed by resetting this Jzonbie.
     * However, it can be overridden by any standard priming.
     *
     * @exception UnsupportedOperationException if response is a {@link DynamicDefaultAppResponse}
     * @param request  the request to match against
     * @param response the response this Jzonbie will return by default
     */
    @Override
    public void prime(AppRequest request, DefaultAppResponse response) {
        if(response instanceof DynamicDefaultAppResponse) throw new UnsupportedOperationException("Priming dynamic default for zombie over HTTP not supported");
        final HttpUriRequest primeZombieRequest = apacheJzonbieRequestFactory.createPrimeZombieForDefaultRequest(request, response.getResponse());
        execute(
                primeZombieRequest,
                httpResponse -> deserializer.deserialize(getHttpResponseBody(httpResponse), ZombiePriming.class),
                format("Failed to prime. %s, %s", request, response)
        );
    }

    @Override
    public List<PrimedMapping> getCurrentPriming() {
        final HttpUriRequest getCurrentPrimingRequest = apacheJzonbieRequestFactory.createGetCurrentPrimingRequest();
        return execute(
                getCurrentPrimingRequest,
                httpResponse -> deserializer.deserializeCollection(getHttpResponseBody(httpResponse), PrimedMapping.class),
                "Failed to get current priming."
        );
    }

    @Override
    public List<Exchange> getHistory() {
        final HttpUriRequest getHistoryRequest = apacheJzonbieRequestFactory.createGetHistoryRequest();
        return execute(
                getHistoryRequest,
                httpResponse -> deserializer.deserializeCollection(getHttpResponseBody(httpResponse), Exchange.class),
                "Failed to get history."
        );
    }

    @Override
    public List<AppRequest> getFailedRequests() {
        final HttpUriRequest getFailedRequestsRequest = apacheJzonbieRequestFactory.createGetFailedRequestsRequest();
        return execute(
                getFailedRequestsRequest,
                httpResponse -> deserializer.deserializeCollection(getHttpResponseBody(httpResponse), AppRequest.class),
                "Failed to get failed requests."
        );
    }

    @Override
    public void verify(AppRequest request, InvocationVerificationCriteria criteria) throws VerificationException {
        final HttpUriRequest verifyRequest = apacheJzonbieRequestFactory.createVerifyRequest(request);
        final CountResult count = execute(
                verifyRequest,
                httpResponse -> deserializer.deserialize(getHttpResponseBody(httpResponse), CountResult.class),
                "Failed to get app request count."
        );
        criteria.verify(count.getCount());
    }

    @Override
    public void reset() {
        final HttpUriRequest resetRequest = apacheJzonbieRequestFactory.createResetRequest();
        execute(
                resetRequest,
                identity(),
                "Failed to reset."
        );
    }

    @Override
    public KeyStore getTruststore() {
        final HttpUriRequest truststoreRequest = apacheJzonbieRequestFactory.createTruststoreRequest();

        return execute(
                truststoreRequest,
                this::convertBytesToKeystore,
                "Failed to obtain truststore."
        );
    }

    private <T> T execute(HttpUriRequest request, Function<HttpResponse, T> mapper, String messageIfFailureOccurs) {
        try(CloseableHttpResponse response = httpClient.execute(request)) {
            return mapper.apply(response);
        } catch (Exception e) {
            throw new JzonbieClientException(messageIfFailureOccurs, e);
        }
    }

    private String getHttpResponseBody(HttpResponse response) {
        try {
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new JzonbieClientException("Could not get body from HTTP response.");
        }
    }

    private KeyStore convertBytesToKeystore(HttpResponse response) {
        if(response.getStatusLine().getStatusCode() != SC_OK) {
            throw new IllegalStateException("Failed to obtain truststore from server");
        }
        try {
            final KeyStore keyStore = KeyStore.getInstance("jks");
            final String content = EntityUtils.toString(response.getEntity());
            final String deserializedContent = deserializer.deserialize(content, String.class);
            final byte[] bytes = getDecoder().decode(deserializedContent);
            keyStore.load(new ByteArrayInputStream(bytes), new char[0]);
            return keyStore;
        } catch(Exception e) {
            throw new RuntimeException("Failed to convert response to keystore", e);
        }
    }
}