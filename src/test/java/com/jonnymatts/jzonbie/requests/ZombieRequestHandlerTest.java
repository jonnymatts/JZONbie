package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.google.common.collect.Multimap;
import com.jonnymatts.jzonbie.model.*;
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

import static java.util.Collections.singletonMap;
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

    @Mock private Multimap<AppRequest, AppResponse> primingContext;

    @Mock private Deserializer deserializer;

    @Mock private PrimedMappingFactory primedMappingFactory;

    @Mock private Request request;

    @Mock private ZombiePriming zombiePriming;

    @Mock private AppRequest zombieRequest;

    @Mock private AppResponse zombieResponse;

    @Fixture private List<ZombiePriming> callHistory;

    @Fixture private List<PrimedMapping> primedRequests;

    private ZombieRequestHandler zombieRequestHandler;

    @Before
    public void setUp() throws Exception {
        zombieRequestHandler = new ZombieRequestHandler(primingContext, callHistory, deserializer, primedMappingFactory);

        when(zombiePriming.getAppRequest()).thenReturn(zombieRequest);
        when(zombiePriming.getAppResponse()).thenReturn(zombieResponse);
    }

    @Test
    public void handleAddsRequestToPrimingContextIfZombieHeaderHasPrimingValue() throws JsonProcessingException {
        when(request.getPath()).thenReturn("path");
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming"));
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(zombiePriming);

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got.getStatusCode()).isEqualTo(CREATED_201);
        assertThat(got.getHeaders()).containsOnly(entry("Content-Type", "application/json"));
        assertThat(got.getBody()).isEqualTo(zombiePriming);

        verify(primingContext).put(zombiePriming.getAppRequest(), zombiePriming.getAppResponse());
    }

    @Test
    public void handleUsesRequestMethodAsPrimingRequestMethodIfNotPresentInPrimedRequest() throws JsonProcessingException {
        when(request.getPath()).thenReturn("path");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming"));
        when(zombieRequest.getMethod()).thenReturn(null);
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(zombiePriming);

        zombieRequestHandler.handle(request);

        verify(zombieRequest).setMethod(request.getMethod());
    }

    @Test
    public void handleUsesRequestPathAsPrimingRequestPathIfNotPresentInPrimedRequest() throws JsonProcessingException {
        when(request.getPath()).thenReturn("path");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming"));
        when(zombieRequest.getPath()).thenReturn(null);
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(zombiePriming);

        zombieRequestHandler.handle(request);

        verify(zombieRequest).setPath(request.getPath());
    }

    @Test
    public void handleReturnsPrimingContextMappingsIfZombieHeaderHasListValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "list"));
        when(primedMappingFactory.create(primingContext)).thenReturn(primedRequests);

        final Response got = zombieRequestHandler.handle(request);

        assertThat(got.getStatusCode()).isEqualTo(OK_200);
        assertThat(got.getHeaders()).containsOnly(entry("Content-Type", "application/json"));
        assertThat(got.getBody()).isEqualTo(primedRequests);
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
}