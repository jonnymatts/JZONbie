package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.Request;
import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.jackson.Deserializer;
import com.jonnymatts.jzonbie.priming.CallHistory;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.priming.PrimingContext;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.CurrentPrimingFileResponseFactory;
import com.jonnymatts.jzonbie.responses.CurrentPrimingFileResponseFactory.FileResponse;
import com.jonnymatts.jzonbie.responses.DefaultingQueue;
import com.jonnymatts.jzonbie.verification.CountResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.responses.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ZombieRequestHandlerTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();
    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private PrimingContext primingContext;
    @Mock private Deserializer deserializer;
    @Mock private CurrentPrimingFileResponseFactory currentPrimingFileResponseFactory;
    @Mock private Request request;
    @Mock private ZombiePriming zombiePriming;
    @Mock private AppRequest zombieRequest;
    @Mock private AppResponse zombieResponse;
    @Mock private FileResponse fileResponse;
    @Mock private PrimedMappingUploader primedMappingUploader;

    @Fixture private String primingFileContent;

    private List<AppRequest> appRequests;
    private List<AppResponse> appResponses;
    private ZombiePriming zombiePriming1;
    private ZombiePriming zombiePriming2;
    private ZombiePriming zombiePriming3;

    private CallHistory callHistory;
    private List<AppRequest> failedRequests;
    private DefaultingQueue defaultingQueue;
    private List<PrimedMapping> primedRequests;
    private ZombieRequestHandler zombieRequestHandler;

    @Before
    public void setUp() {
        appRequests = asList(
                get("/1").build(),
                get("/2").build(),
                get("/3").build()
        );

        appResponses = asList(
                ok().build(),
                ok().build(),
                ok().build()
        );

        zombiePriming1 = new ZombiePriming(
                get("/4").build(),
                ok().build()
        );

        zombiePriming2 = new ZombiePriming(
                get("/5").build(),
                ok().build()
        );

        zombiePriming3 = new ZombiePriming(
                get("/6").build(),
                ok().build()
        );

        callHistory = new CallHistory(new ArrayList<ZombiePriming>(){{
            add(zombiePriming1);
            add(zombiePriming2);
            add(zombiePriming3);
        }});

        failedRequests = new ArrayList<AppRequest>(){{
            add(appRequests.get(0));
        }};

        zombieRequestHandler = new ZombieRequestHandler("zombie", primingContext, callHistory, failedRequests, deserializer, currentPrimingFileResponseFactory, primedMappingUploader);
        defaultingQueue = new DefaultingQueue() {{
            add(appResponses);
        }};
        primedRequests = appRequests.stream().map(request -> new PrimedMapping(request, defaultingQueue)).collect(toList());

        when(zombiePriming.getRequest()).thenReturn(zombieRequest);
        when(zombiePriming.getResponse()).thenReturn(zombieResponse);
    }

    @Test
    public void handleAddsRequestToPrimingContextIfZombieHeaderHasPrimingValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming"));
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(zombiePriming);
        when(zombieRequest.getPath()).thenReturn("path");
        when(zombieRequest.getMethod()).thenReturn("method");

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(CREATED_201, zombiePriming));

        verify(primingContext).add(zombiePriming.getRequest(), zombiePriming.getResponse());
    }

    @Test
    public void handleAddsDefaultRequestToPrimingContextIfZombieHeaderHasDefaultPrimingValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming-default"));
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(zombiePriming);
        when(zombieRequest.getPath()).thenReturn("path");
        when(zombieRequest.getMethod()).thenReturn("method");

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(CREATED_201, zombiePriming));

        verify(primingContext).addDefault(zombiePriming.getRequest(), staticDefault(zombiePriming.getResponse()));
    }

    @Test
    public void handleAddsPrimingFromFileToPrimingContextIfZombieHeaderHasPrimingFileValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming-file"));
        when(request.getPrimingFileContent()).thenReturn(primingFileContent);
        when(deserializer.deserializeCollection(primingFileContent, PrimedMapping.class)).thenReturn(primedRequests);

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(CREATED_201, primedRequests));

        verify(primedMappingUploader).upload(primedRequests);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleThrowsExceptionIfMethodNotPresentInPrimedRequest() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming"));
        when(zombieRequest.getMethod()).thenReturn(null);
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(zombiePriming);

        zombieRequestHandler.handle(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleThrowsExceptionIfPathNotPresentInPrimedRequest() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming"));
        when(zombieRequest.getMethod()).thenReturn("GET");
        when(zombieRequest.getPath()).thenReturn(null);
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(zombiePriming);

        zombieRequestHandler.handle(request);
    }

    @Test
    public void handleReturnsPrimingContextMappingsIfZombieHeaderHasCurrentValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "current"));
        when(primingContext.getCurrentPriming()).thenReturn(primedRequests);

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(OK_200, primedRequests));
    }

    @Test
    public void handleReturnsFileResponseIfZombieHeaderHasCurrentFileValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "current-file"));
        when(primingContext.getCurrentPriming()).thenReturn(primedRequests);
        when(currentPrimingFileResponseFactory.create(primedRequests)).thenReturn(fileResponse);

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(fileResponse);
    }

    @Test
    public void handleClearsPrimingContextCallHistoryAndFailedRequestsIfZombieHeaderHasResetValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "reset"));

        assertThat(callHistory.getEntries()).isNotEmpty();
        assertThat(failedRequests).isNotEmpty();

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(OK_200, singletonMap("message", "Zombie Reset")));
        assertThat(callHistory.getEntries()).isEmpty();
        assertThat(failedRequests).isEmpty();

        verify(primingContext).reset();
    }

    @Test
    public void handleReturnsCallHistoryIfZombieHeaderHasHistoryValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "history"));

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(OK_200, callHistory));
    }

    @Test
    public void handleReturnsFailedRequestsIfZombieHeaderHasFailedValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "failed"));

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(OK_200, failedRequests));
    }

    @Test
    public void handleReturnsRequestCountForMatchingRequestResultIfZombieHeaderHasCountValue() throws Exception {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "count"));
        when(deserializer.deserialize(request, AppRequest.class)).thenReturn(zombiePriming1.getRequest());

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(OK_200, new CountResult(1)));
    }

    @Test
    public void handleThrowsRuntimeExceptionIfZombieHeaderHasUnknownValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "unknownValue"));

        assertThatThrownBy(() -> zombieRequestHandler.handle(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("unknownValue");
    }

    @Test
    public void zombieHeaderNameCanBeSet() throws JsonProcessingException {
        zombieRequestHandler = new ZombieRequestHandler("name", primingContext, callHistory, failedRequests, deserializer, currentPrimingFileResponseFactory, primedMappingUploader);

        when(request.getHeaders()).thenReturn(singletonMap("name", "history"));

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got).isEqualTo(new ZombieResponse(OK_200, callHistory));
    }
}