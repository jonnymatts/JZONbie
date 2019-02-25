package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.priming.AppRequestFactory;
import com.jonnymatts.jzonbie.priming.CallHistory;
import com.jonnymatts.jzonbie.priming.PrimingContext;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.responses.Response;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppRequestHandlerTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private PrimingContext primingContext;

    @Mock private CallHistory callHistory;

    @Mock private List<AppRequest> failedRequests;

    @Mock private AppRequestFactory appRequestFactory;

    @Mock private Request request;

    private ZombiePriming zombiePriming;

    private AppRequestHandler appRequestHandler;

    private AppRequest appRequest;

    private AppResponse appResponse;

    @Before
    public void setUp() throws Exception {
        appRequestHandler = new AppRequestHandler(primingContext, callHistory, failedRequests, appRequestFactory);

        appRequest = AppRequestBuilderUtil.getFixturedAppRequest();
        appResponse = AppResponseBuilderUtil.getFixturedAppResponse();

        zombiePriming = new ZombiePriming(appRequest, appResponse);

        when(appRequestFactory.create(request)).thenReturn(appRequest);
        when(primingContext.getResponse(appRequest))
                .thenReturn(of(appResponse));
    }

    @Test
    public void handleReturnsPrimedResponseIfPrimingKeyExistsInPrimingContext() throws JsonProcessingException {
        final Response got = appRequestHandler.handle(request);

        assertThat(got.getStatusCode()).isEqualTo(appResponse.getStatusCode());
        assertThat(got.getHeaders()).containsAllEntriesOf(appResponse.getHeaders());
        assertThat(got.getBody()).isEqualTo(appResponse.getBody());
    }

    @Test
    public void handleReturnsPrimedResponseIfPrimingKeyExistsWithStringContentBody() throws JsonProcessingException {


        final Response got = appRequestHandler.handle(request);

        assertThat(got.getStatusCode()).isEqualTo(appResponse.getStatusCode());
        assertThat(got.getHeaders()).containsAllEntriesOf(appResponse.getHeaders());
        assertThat(got.getBody()).isEqualTo(appResponse.getBody());
    }

    @Test
    public void handleAddsPrimingRequestToCallHistory() throws JsonProcessingException {
        appRequestHandler.handle(request);

        verify(callHistory).add(zombiePriming);
    }

    @Test
    public void handleReturnsErrorResponseIfPrimingIsNotFound() throws Exception {
        when(primingContext.getResponse(appRequest)).thenReturn(empty());

        expectedException.expect(PrimingNotFoundException.class);
        expectedException.expect(Matchers.hasProperty("request", equalTo(appRequest)));

        appRequestHandler.handle(request);
    }

    @Test
    public void handleAddsRequestToFailedRequestsIfPrimingIsNotFound() throws Exception {
        when(primingContext.getResponse(appRequest)).thenReturn(empty());

        try{
            appRequestHandler.handle(request);
        } catch (Exception e) {
            verify(failedRequests).add(appRequest);
        }
    }
}