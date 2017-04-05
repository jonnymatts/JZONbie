package com.jonnymatts.jzonbie.client;

import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.util.Deserializer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static com.jonnymatts.jzonbie.response.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApacheJzonbieHttpClientTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();
    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private ApacheJzonbieRequestFactory apacheJzonbieRequestFactory;
    @Mock private CloseableHttpClient httpClient;
    @Mock private Deserializer deserializer;
    @Mock private AppRequest appRequest;
    @Mock private AppResponse appResponse;
    @Mock private HttpUriRequest httpRequest;
    @Mock private CloseableHttpResponse httpResponse;

    private RuntimeException runtimeException;

    private ApacheJzonbieHttpClient jzonbieHttpClient;

    @Before
    public void setUp() throws Exception {
        jzonbieHttpClient = new ApacheJzonbieHttpClient(httpClient, apacheJzonbieRequestFactory, deserializer);
        runtimeException = new RuntimeException();
    }

    @Test
    public void primeZombieReturnsPrimingRequest() throws Exception {
        final ZombiePriming zombiePriming = new ZombiePriming();

        when(apacheJzonbieRequestFactory.createPrimeZombieRequest(appRequest, appResponse)).thenReturn(httpRequest);
        when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
        when(deserializer.deserialize(httpResponse, ZombiePriming.class)).thenReturn(zombiePriming);

        final ZombiePriming got = jzonbieHttpClient.primeZombie(appRequest, appResponse);

        assertThat(got).isEqualTo(zombiePriming);
    }

    @Test
    public void primeZombieForDefaultReturnsPrimingRequest() throws Exception {
        final ZombiePriming zombiePriming = new ZombiePriming();

        when(apacheJzonbieRequestFactory.createPrimeZombieForDefaultRequest(appRequest, appResponse)).thenReturn(httpRequest);
        when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
        when(deserializer.deserialize(httpResponse, ZombiePriming.class)).thenReturn(zombiePriming);

        final ZombiePriming got = jzonbieHttpClient.primeZombieForDefault(appRequest, staticDefault(appResponse));

        assertThat(got).isEqualTo(zombiePriming);
    }

    @Test
    public void primeZombieThrowsRuntimeExceptionIfHttpClientThrowsException() throws Exception {
        when(apacheJzonbieRequestFactory.createPrimeZombieRequest(appRequest, appResponse)).thenReturn(httpRequest);
        when(httpClient.execute(httpRequest)).thenThrow(runtimeException);

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(runtimeException));

        jzonbieHttpClient.primeZombie(appRequest, appResponse);
    }

    @Test
    public void getCurrentPrimingReturnsPrimedMappings() throws Exception {
        final List<PrimedMapping> primedMappings = emptyList();

        when(apacheJzonbieRequestFactory.createGetCurrentPrimingRequest()).thenReturn(httpRequest);
        when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
        when(deserializer.deserializeCollection(httpResponse, PrimedMapping.class)).thenReturn(primedMappings);

        final List<PrimedMapping> got = jzonbieHttpClient.getCurrentPriming();

        assertThat(got).isEqualTo(primedMappings);
    }

    @Test
    public void getCurrentPrimingThrowsRuntimeExceptionIfHttpClientThrowsException() throws Exception {
        when(apacheJzonbieRequestFactory.createGetCurrentPrimingRequest()).thenReturn(httpRequest);
        when(httpClient.execute(httpRequest)).thenThrow(runtimeException);

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(runtimeException));

        jzonbieHttpClient.getCurrentPriming();
    }

    @Test
    public void getHistoryReturnsCallHistory() throws Exception {
        final List<ZombiePriming> zombiePrimings = emptyList();

        when(apacheJzonbieRequestFactory.createGetHistoryRequest()).thenReturn(httpRequest);
        when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
        when(deserializer.deserializeCollection(httpResponse, ZombiePriming.class)).thenReturn(zombiePrimings);

        final List<ZombiePriming> got = jzonbieHttpClient.getHistory();

        assertThat(got).isEqualTo(zombiePrimings);
    }

    @Test
    public void getHistoryThrowsRuntimeExceptionIfHttpClientThrowsException() throws Exception {
        when(apacheJzonbieRequestFactory.createGetHistoryRequest()).thenReturn(httpRequest);
        when(httpClient.execute(httpRequest)).thenThrow(runtimeException);

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(runtimeException));

        jzonbieHttpClient.getHistory();
    }

    @Test
    public void resetExecutesResetRequest() throws Exception {
        when(apacheJzonbieRequestFactory.createResetRequest()).thenReturn(httpRequest);

        jzonbieHttpClient.reset();

        verify(httpClient).execute(httpRequest);
    }

    @Test
    public void resetThrowsRuntimeExceptionIfHttpClientThrowsException() throws Exception {
        when(apacheJzonbieRequestFactory.createResetRequest()).thenReturn(httpRequest);
        when(httpClient.execute(httpRequest)).thenThrow(runtimeException);

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(runtimeException));

        jzonbieHttpClient.reset();
    }
}