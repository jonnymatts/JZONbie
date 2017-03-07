package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.JzonbieOptions;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.response.CurrentPrimingFileResponseFactory;
import com.jonnymatts.jzonbie.response.CurrentPrimingFileResponseFactory.FileResponse;
import com.jonnymatts.jzonbie.response.DefaultResponse.StaticDefaultResponse;
import com.jonnymatts.jzonbie.response.DefaultingQueue;
import com.jonnymatts.jzonbie.response.Response;
import com.jonnymatts.jzonbie.util.Deserializer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
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

    @Fixture private List<ZombiePriming> callHistory;
    @Fixture private List<AppRequest> appRequests;
    @Fixture private List<AppResponse> appResponses;
    @Fixture private JzonbieOptions jzonbieOptions;
    @Fixture private String primingFileContent;

    private DefaultingQueue<AppResponse> defaultingQueue;
    private List<PrimedMapping> primedRequests;
    private ZombieRequestHandler zombieRequestHandler;

    @Before
    public void setUp() throws Exception {
        zombieRequestHandler = new ZombieRequestHandler(jzonbieOptions, primingContext, callHistory, deserializer, currentPrimingFileResponseFactory);
        defaultingQueue = new DefaultingQueue<AppResponse>() {{
            add(appResponses);
        }};
        primedRequests = appRequests.stream().map(request -> new PrimedMapping(request, defaultingQueue)).collect(toList());

        when(zombiePriming.getAppRequest()).thenReturn(zombieRequest);
        when(zombiePriming.getAppResponse()).thenReturn(zombieResponse);
    }

    @Test
    public void handleAddsRequestToPrimingContextIfZombieHeaderHasPrimingValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming"));
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(zombiePriming);
        when(zombieRequest.getPath()).thenReturn("path");
        when(zombieRequest.getMethod()).thenReturn("method");

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got.getStatusCode()).isEqualTo(CREATED_201);
        assertThat(got.getHeaders()).containsOnly(entry("Content-Type", "application/json"));
        assertThat(got.getBody()).isEqualTo(zombiePriming);

        verify(primingContext).add(zombiePriming.getAppRequest(), zombiePriming.getAppResponse());
    }

    @Test
    public void handleAddsDefaultRequestToPrimingContextIfZombieHeaderHasDefaultPrimingValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming-default"));
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(zombiePriming);
        when(zombieRequest.getPath()).thenReturn("path");
        when(zombieRequest.getMethod()).thenReturn("method");

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got.getStatusCode()).isEqualTo(CREATED_201);
        assertThat(got.getHeaders()).containsOnly(entry("Content-Type", "application/json"));
        assertThat(got.getBody()).isEqualTo(zombiePriming);

        verify(primingContext).addDefault(zombiePriming.getAppRequest(), new StaticDefaultResponse<>(zombiePriming.getAppResponse()));
    }

    @Test
    public void handleAddsPrimingFromFileToPrimingContextIfZombieHeaderHasPrimingFileValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming-file"));
        when(request.getPrimingFileContent()).thenReturn(primingFileContent);
        when(deserializer.deserializeCollection(primingFileContent, PrimedMapping.class)).thenReturn(primedRequests);

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got.getStatusCode()).isEqualTo(CREATED_201);
        assertThat(got.getHeaders()).containsOnly(entry("Content-Type", "application/json"));
        assertThat(got.getBody()).isEqualTo(primedRequests);

        primedRequests.forEach(primedMapping ->
                appResponses.forEach(appResponse ->
                        primingContext.add(primedMapping.getAppRequest(), appResponse)
                )
        );
    }

    @Test
    public void handleAddsPrimingFromFileToPrimingContextWithDefaultIfZombieHeaderHasPrimingFileValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming-file"));
        when(request.getPrimingFileContent()).thenReturn(primingFileContent);
        final List<PrimedMapping> mappings = singletonList(new PrimedMapping(
                zombieRequest,
                new DefaultingQueue<AppResponse>() {{
                    setDefault(new StaticDefaultResponse<>(zombieResponse));
                }}
        ));
        when(deserializer.deserializeCollection(primingFileContent, PrimedMapping.class)).thenReturn(mappings);

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got.getStatusCode()).isEqualTo(CREATED_201);
        assertThat(got.getHeaders()).containsOnly(entry("Content-Type", "application/json"));
        assertThat(got.getBody()).isEqualTo(mappings);

        verify(primingContext).addDefault(zombieRequest, new StaticDefaultResponse<>(zombieResponse));
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
        when(zombieRequest.getPath()).thenReturn(null);
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(zombiePriming);

        zombieRequestHandler.handle(request);
    }

    @Test
    public void handleReturnsPrimingContextMappingsIfZombieHeaderHasCurrentValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "current"));
        when(primingContext.getCurrentPriming()).thenReturn(primedRequests);

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got.getStatusCode()).isEqualTo(OK_200);
        assertThat(got.getHeaders()).containsOnly(entry("Content-Type", "application/json"));
        assertThat(got.getBody()).isEqualTo(primedRequests);
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
    public void handleClearsPrimingContextAndCallHistoryIfZombieHeaderHasResetValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "reset"));

        assertThat(callHistory).isNotEmpty();

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got.getStatusCode()).isEqualTo(OK_200);
        assertThat(got.getBody()).isEqualTo("Zombie Reset");
        assertThat(callHistory).isEmpty();

        verify(primingContext).clear();
    }

    @Test
    public void handleReturnsCallHistoryIfZombieHeaderHasHistoryValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "history"));

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got.getStatusCode()).isEqualTo(OK_200);
        assertThat(got.getHeaders()).containsOnly(entry("Content-Type", "application/json"));
        assertThat(got.getBody()).isEqualTo(callHistory);
    }

    @Test
    public void handleThrowsRuntimeExceptionIfZombieHeaderHasUnknownValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "unknownValue"));

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("unknownValue");

        zombieRequestHandler.handle(request);
    }

    @Test
    public void zombieHeaderNameCanBeSet() throws JsonProcessingException {
        zombieRequestHandler = new ZombieRequestHandler(jzonbieOptions, primingContext, callHistory, deserializer, currentPrimingFileResponseFactory);

        when(request.getHeaders()).thenReturn(singletonMap(jzonbieOptions.getZombieHeaderName(), "history"));

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got.getStatusCode()).isEqualTo(OK_200);
        assertThat(got.getHeaders()).containsOnly(entry("Content-Type", "application/json"));
        assertThat(got.getBody()).isEqualTo(callHistory);
    }
}