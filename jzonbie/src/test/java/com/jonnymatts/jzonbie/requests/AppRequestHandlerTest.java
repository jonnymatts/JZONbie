package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jonnymatts.jzonbie.Request;
import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.priming.AppRequestFactory;
import com.jonnymatts.jzonbie.priming.CallHistory;
import com.jonnymatts.jzonbie.priming.PrimingContext;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.responses.AppResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppRequestHandlerTest {

    @Mock private PrimingContext primingContext;
    @Mock private CallHistory callHistory;
    @Mock private List<AppRequest> failedRequests;
    @Mock private AppRequestFactory appRequestFactory;
    @Mock private Request request;

    private ZombiePriming zombiePriming;

    private AppRequestHandler appRequestHandler;

    private AppRequest appRequest;

    private AppResponse appResponse;

    @BeforeEach
    void setUp() throws Exception {
        appRequestHandler = new AppRequestHandler(primingContext, callHistory, failedRequests, appRequestFactory);

        appRequest = get("/");
        appResponse = ok();

        zombiePriming = new ZombiePriming(appRequest, appResponse);

        when(appRequestFactory.create(request)).thenReturn(appRequest);
        when(primingContext.getResponse(appRequest))
                .thenReturn(of(appResponse));
    }

    @Test
    void handleReturnsPrimedResponseIfPrimingKeyExistsInPrimingContext() throws JsonProcessingException {
        final Response got = appRequestHandler.handle(request);

        assertThat(got).isEqualTo(appResponse);
    }

    @Test
    void handleAddsPrimingRequestToCallHistory() throws JsonProcessingException {
        appRequestHandler.handle(request);

        verify(callHistory).add(zombiePriming);
    }

    @Test
    void handleThrowsPrimingNotFoundExceptionIfPrimingIsNotFound() throws Exception {
        when(primingContext.getResponse(appRequest)).thenReturn(empty());

        assertThatThrownBy(() -> appRequestHandler.handle(request))
                .isExactlyInstanceOf(PrimingNotFoundException.class)
                .hasFieldOrPropertyWithValue("request", appRequest);
    }

    @Test
    void handleAddsRequestToFailedRequestsIfPrimingIsNotFound() throws Exception {
        when(primingContext.getResponse(appRequest)).thenReturn(empty());

        try{
            appRequestHandler.handle(request);
        } catch (Exception e) {
            verify(failedRequests).add(appRequest);
        }
    }
}