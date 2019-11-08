package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.Request;
import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.history.CallHistory;
import com.jonnymatts.jzonbie.history.Exchange;
import com.jonnymatts.jzonbie.history.FixedCapacityCache;
import com.jonnymatts.jzonbie.jackson.Deserializer;
import com.jonnymatts.jzonbie.metadata.MetaDataContext;
import com.jonnymatts.jzonbie.priming.AppRequestFactory;
import com.jonnymatts.jzonbie.priming.PrimingContext;
import com.jonnymatts.jzonbie.responses.AppResponse;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static com.jonnymatts.jzonbie.metadata.MetaDataTag.ENDPOINT_REQUEST_COUNT;
import static com.jonnymatts.jzonbie.metadata.MetaDataTag.ENDPOINT_REQUEST_PERSISTENT_COUNT;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppRequestHandlerTest {

    private static final String UNKNOWN_PATH = "/unknownPath";
    private static final String GET_METHOD = "GET";
    private static final String RESPONSE_BODY = "Hello every-Body";

    private CallHistory callHistory;
    private FixedCapacityCache<AppRequest> failedRequests;
    private AppRequestHandler appRequestHandler;
    private MetaDataContext metaDataContext;

    @Mock private Request request;

    @BeforeEach
    void setUp() {
        PrimingContext primingContext = new PrimingContext();
        primingContext.add(get("/primed"), AppResponse.ok().withBody(RESPONSE_BODY));
        AppRequestFactory appRequestFactory = new AppRequestFactory(new Deserializer());

        callHistory = new CallHistory(3, Files.newTemporaryFile());
        failedRequests = new FixedCapacityCache<>(3);
        appRequestHandler = new AppRequestHandler(primingContext, callHistory, failedRequests, appRequestFactory);
        metaDataContext = new MetaDataContext("http", "http://url.com", 80, "/path/here", Collections.emptyMap(), Collections.emptyMap(), GET_METHOD, RESPONSE_BODY);

        when(request.getPath()).thenReturn("/primed");
        when(request.getMethod()).thenReturn("GET");
    }

    @Test
    void handleReturnsPrimedResponseIfPrimingKeyExistsInPrimingContext() {
        final Response got = appRequestHandler.handle(request, metaDataContext);

        assertThat(got.getBody().getContent()).isEqualTo(RESPONSE_BODY);
    }

    @Test
    void handleAddsPrimingRequestToCallHistory() {
        Exchange expectedExchange = new Exchange(get("/primed"), AppResponse.ok().withBody(RESPONSE_BODY));

        appRequestHandler.handle(request, metaDataContext);

        assertThat(callHistory.getValues().get(0)).isEqualTo(expectedExchange);
        assertThat(callHistory.getValues().size()).isEqualTo(1);
    }

    @Test
    void handleThrowsPrimingNotFoundExceptionIfPrimingIsNotFound() {
        when(request.getPath()).thenReturn(UNKNOWN_PATH);

        assertThatThrownBy(() -> appRequestHandler.handle(request, metaDataContext))
                .isExactlyInstanceOf(PrimingNotFoundException.class)
                .hasFieldOrPropertyWithValue("request", new AppRequest(GET_METHOD, UNKNOWN_PATH));
    }

    @Test
    void handleAddsRequestToFailedRequestsIfPrimingIsNotFound() {
        when(request.getPath()).thenReturn(UNKNOWN_PATH);

        try {
            appRequestHandler.handle(request, metaDataContext);
        } catch (Exception ignored) { }

        assertThat(failedRequests.getValues().get(0)).isEqualTo(new AppRequest(GET_METHOD, UNKNOWN_PATH));
    }

    @Test
    void populateMetaDataWithEndpointRequestCount() {
        appRequestHandler.handle(request, metaDataContext);

        assertThat(metaDataContext.getContext().get(ENDPOINT_REQUEST_COUNT.toString())).isEqualTo(1);
    }

    @Test
    void populateMetaDataWithPersistedEndpointRequestCount() {
        appRequestHandler.handle(request, metaDataContext);

        assertThat(metaDataContext.getContext().get(ENDPOINT_REQUEST_PERSISTENT_COUNT.toString())).isEqualTo(1);
    }
}