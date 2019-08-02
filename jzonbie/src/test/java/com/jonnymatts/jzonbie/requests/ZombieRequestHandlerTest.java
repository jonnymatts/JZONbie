package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flextrade.jfixture.JFixture;
import com.jonnymatts.jzonbie.Request;
import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.history.CallHistory;
import com.jonnymatts.jzonbie.history.Exchange;
import com.jonnymatts.jzonbie.history.FixedCapacityCache;
import com.jonnymatts.jzonbie.jackson.Deserializer;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.priming.PrimingContext;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.CurrentPrimingFileResponseFactory;
import com.jonnymatts.jzonbie.responses.CurrentPrimingFileResponseFactory.FileResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultingQueue;
import com.jonnymatts.jzonbie.ssl.HttpsSupport;
import com.jonnymatts.jzonbie.verification.CountResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse.staticDefault;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZombieRequestHandlerTest {

    private static final JFixture FIXTURE = new JFixture();

    @Mock private PrimingContext primingContext;
    @Mock private Deserializer deserializer;
    @Mock private CurrentPrimingFileResponseFactory currentPrimingFileResponseFactory;
    @Mock private Request request;
    @Mock private ZombiePriming zombiePriming;
    @Mock private AppRequest zombieRequest;
    @Mock private AppResponse zombieResponse;
    @Mock private FileResponse fileResponse;
    @Mock private PrimedMappingUploader primedMappingUploader;

    private static final String primingFileContent = FIXTURE.create(String.class);

    private List<AppRequest> appRequests;
    private List<AppResponse> appResponses;
    private ZombiePriming zombiePriming1;
    private ZombiePriming zombiePriming2;
    private ZombiePriming zombiePriming3;
    private Exchange exchange1;
    private Exchange exchange2;
    private Exchange exchange3;

    private CallHistory callHistory;
    private FixedCapacityCache<AppRequest> failedRequests;
    private DefaultingQueue defaultingQueue;
    private List<PrimedMapping> primedRequests;
    private ZombieRequestHandler zombieRequestHandler;

    @BeforeEach
    void setUp() {
        appRequests = asList(
                get("/1"),
                get("/2"),
                get("/3")
        );

        final AppResponse response = ok();
        appResponses = asList(
                response,
                response,
                response
        );

        final AppRequest request1 = get("/4");
        zombiePriming1 = new ZombiePriming(request1, response);
        exchange1 = new Exchange(request1, response);

        final AppRequest request2 = get("/5");
        zombiePriming2 = new ZombiePriming(request2, response);
        exchange2 = new Exchange(request2, response);

        final AppRequest request3 = get("/6");
        zombiePriming3 = new ZombiePriming(request3, response);
        exchange3 = new Exchange(request3, response);

        callHistory = new CallHistory(100);
        callHistory.add(exchange1);
        callHistory.add(exchange2);
        callHistory.add(exchange3);

        failedRequests = new FixedCapacityCache<>(100);
        failedRequests.add(appRequests.get(0));

        zombieRequestHandler = new ZombieRequestHandler("zombie", primingContext, callHistory, failedRequests, deserializer, currentPrimingFileResponseFactory, primedMappingUploader, new HttpsSupport());
        defaultingQueue = new DefaultingQueue() {{
            add(appResponses);
        }};
        primedRequests = appRequests.stream().map(request -> new PrimedMapping(request, defaultingQueue)).collect(toList());

        lenient().when(zombiePriming.getRequest()).thenReturn(zombieRequest);
        lenient().when(zombiePriming.getResponse()).thenReturn(zombieResponse);
    }

    @Test
    void handleAddsRequestToPrimingContextIfZombieHeaderHasPrimingValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming"));
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(zombiePriming);
        when(zombieRequest.getPath()).thenReturn("path");
        when(zombieRequest.getMethod()).thenReturn("method");

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(CREATED_201, zombiePriming));

        verify(primingContext).add(zombiePriming.getRequest(), zombiePriming.getResponse());
    }

    @Test
    void handleAddsDefaultRequestToPrimingContextIfZombieHeaderHasDefaultPrimingValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming-default"));
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(zombiePriming);
        when(zombieRequest.getPath()).thenReturn("path");
        when(zombieRequest.getMethod()).thenReturn("method");

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(CREATED_201, zombiePriming));

        verify(primingContext).addDefault(zombiePriming.getRequest(), staticDefault(zombiePriming.getResponse()));
    }

    @Test
    void handleAddsPrimingFromFileToPrimingContextIfZombieHeaderHasPrimingFileValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming-file"));
        when(request.getPrimingFileContent()).thenReturn(primingFileContent);
        when(deserializer.deserializeCollection(primingFileContent, PrimedMapping.class)).thenReturn(primedRequests);

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(CREATED_201, primedRequests));

        verify(primedMappingUploader).upload(primedRequests);
    }

    @Test
    void handleThrowsExceptionIfMethodNotPresentInPrimedRequest() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming"));
        when(zombieRequest.getMethod()).thenReturn(null);
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(zombiePriming);

        assertThatThrownBy(() -> zombieRequestHandler.handle(request))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void handleThrowsExceptionIfPathNotPresentInPrimedRequest() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming"));
        when(zombieRequest.getMethod()).thenReturn("GET");
        when(zombieRequest.getPath()).thenReturn(null);
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(zombiePriming);

        assertThatThrownBy(() -> zombieRequestHandler.handle(request))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void handleReturnsPrimingContextMappingsIfZombieHeaderHasCurrentValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "current"));
        when(primingContext.getCurrentPriming()).thenReturn(primedRequests);

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(OK_200, primedRequests));
    }

    @Test
    void handleReturnsFileResponseIfZombieHeaderHasCurrentFileValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "current-file"));
        when(primingContext.getCurrentPriming()).thenReturn(primedRequests);
        when(currentPrimingFileResponseFactory.create(primedRequests)).thenReturn(fileResponse);

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(fileResponse);
    }

    @Test
    void handleClearsPrimingContextCallHistoryAndFailedRequestsIfZombieHeaderHasResetValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "reset"));

        assertThat(callHistory.getValues()).isNotEmpty();
        assertThat(failedRequests.getValues()).isNotEmpty();

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(OK_200, singletonMap("message", "Zombie Reset")));
        assertThat(callHistory.getValues()).isEmpty();
        assertThat(failedRequests.getValues()).isEmpty();

        verify(primingContext).reset();
    }

    @Test
    void handleReturnsCallHistoryIfZombieHeaderHasHistoryValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "history"));

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(OK_200, callHistory));
    }

    @Test
    void handleReturnsFailedRequestsIfZombieHeaderHasFailedValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "failed"));

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(OK_200, failedRequests));
    }

    @Test
    void handleReturnsRequestCountForMatchingRequestResultIfZombieHeaderHasCountValue() throws Exception {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "count"));
        when(deserializer.deserialize(request, AppRequest.class)).thenReturn(zombiePriming1.getRequest());

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(OK_200, new CountResult(1)));
    }

    @Test
    void handleThrowsRuntimeExceptionIfZombieHeaderHasUnknownValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "unknownValue"));

        assertThatThrownBy(() -> zombieRequestHandler.handle(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("unknownValue");
    }

    @Test
    void zombieHeaderNameCanBeSet() throws JsonProcessingException {
        zombieRequestHandler = new ZombieRequestHandler("name", primingContext, callHistory, failedRequests, deserializer, currentPrimingFileResponseFactory, primedMappingUploader, new HttpsSupport());

        when(request.getHeaders()).thenReturn(singletonMap("name", "history"));

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(OK_200, callHistory));
    }
}