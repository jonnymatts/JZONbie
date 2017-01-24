package com.jonnymatts.jzonbie.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.google.common.collect.Multimap;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.util.Deserializer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Response;

import java.util.List;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ZombieRequestHandlerTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private Multimap<ZombieRequest, ZombieResponse> primingContext;

    @Mock private Deserializer deserializer;

    @Mock private PrimedMappingFactory primedMappingFactory;

    @Mock private Request request;

    @Mock private Response response;

    @Mock private ZombiePriming ZombiePriming;

    @Mock private ZombieRequest zombieRequest;

    @Mock private ZombieResponse zombieResponse;

    @Fixture private List<ZombiePriming> callHistory;

    @Fixture private List<PrimedMapping> primedRequests;

    @Fixture private String primingRequestString;

    private ZombieRequestHandler zombieRequestHandler;

    @Before
    public void setUp() throws Exception {
        zombieRequestHandler = new ZombieRequestHandler(primingContext, callHistory, deserializer, primedMappingFactory);

        when(ZombiePriming.getZombieRequest()).thenReturn(zombieRequest);
        when(ZombiePriming.getZombieResponse()).thenReturn(zombieResponse);
    }

    @Test
    public void handleAddsRequestToPrimingContextIfZombieHeaderHasPrimingValue() throws JsonProcessingException {
        when(request.getPath()).thenReturn("path");
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming"));
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(ZombiePriming);

        final Object got = zombieRequestHandler.handle(request, response);

        assertThat(got).isEqualTo(ZombiePriming);

        verify(primingContext).put(ZombiePriming.getZombieRequest(), ZombiePriming.getZombieResponse());
        verify(response).status(CREATED_201);
    }

    @Test
    public void handleUsesRequestMethodAsPrimingRequestMethodIfNotPresentInPrimedRequest() throws JsonProcessingException {
        when(request.getPath()).thenReturn("path");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming"));
        when(zombieRequest.getMethod()).thenReturn(null);
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(ZombiePriming);

        zombieRequestHandler.handle(request, response);

        verify(zombieRequest).setMethod(request.getMethod());
    }

    @Test
    public void handleUsesRequestPathAsPrimingRequestPathIfNotPresentInPrimedRequest() throws JsonProcessingException {
        when(request.getPath()).thenReturn("path");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "priming"));
        when(zombieRequest.getPath()).thenReturn(null);
        when(deserializer.deserialize(request, ZombiePriming.class)).thenReturn(ZombiePriming);

        zombieRequestHandler.handle(request, response);

        verify(zombieRequest).setPath(request.getPath());
        verify(response).status(CREATED_201);
        verify(response).header("Content-Type", "application/json");
    }

    @Test
    public void handleReturnsPrimingContextMappingsIfZombieHeaderHasListValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "list"));
        when(primedMappingFactory.create(primingContext)).thenReturn(primedRequests);

        final Object got = zombieRequestHandler.handle(request, response);

        assertThat(got).isEqualTo(primedRequests);

        verify(response).status(OK_200);
        verify(response).header("Content-Type", "application/json");
    }

    @Test
    public void handleClearsPrimingContextAndCallHistoryIfZombieHeaderHasResetValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "reset"));

        assertThat(callHistory).isNotEmpty();

        final Object got = zombieRequestHandler.handle(request, response);

        assertThat(got).isEqualTo("Zombie Reset");
        assertThat(callHistory).isEmpty();

        verify(primingContext).clear();
        verify(response).status(OK_200);
    }

    @Test
    public void handleReturnsCallHistoryIfZombieHeaderHasHistoryValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "history"));

        final Object got = zombieRequestHandler.handle(request, response);

        assertThat(got).isEqualTo(callHistory);

        verify(response).status(OK_200);
        verify(response).header("Content-Type", "application/json");
    }

    @Test
    public void handleThrowsRuntimeExceptionIfZombieHeaderHasUnknownValue() throws JsonProcessingException {
        when(request.getHeaders()).thenReturn(singletonMap("zombie", "unknownValue"));

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("unknownValue");

        zombieRequestHandler.handle(request, response);
    }
}