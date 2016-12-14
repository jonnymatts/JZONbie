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
import spark.Request;
import spark.Response;

import java.util.List;

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

    @Mock private PrimingRequest PrimingRequest;

    @Mock private ZombieRequest zombieRequest;

    @Mock private ZombieResponse zombieResponse;

    @Fixture private List<PrimingRequest> callHistory;

    @Fixture private List<PrimedMapping> primedRequests;

    @Fixture private String primingRequestString;

    private ZombieRequestHandler zombieRequestHandler;

    @Before
    public void setUp() throws Exception {
        zombieRequestHandler = new ZombieRequestHandler(primingContext, callHistory, deserializer, primedMappingFactory);

        when(PrimingRequest.getZombieRequest()).thenReturn(zombieRequest);
        when(PrimingRequest.getZombieResponse()).thenReturn(zombieResponse);
    }

    @Test
    public void handleAddsRequestToPrimingContextIfZombieHeaderHasPrimingValue() throws JsonProcessingException {
        when(request.pathInfo()).thenReturn("path");
        when(request.headers("zombie")).thenReturn("priming");
        when(deserializer.deserialize(request, PrimingRequest.class)).thenReturn(PrimingRequest);

        final Object got = zombieRequestHandler.handle(request, response);

        assertThat(got).isEqualTo(PrimingRequest);

        verify(primingContext).put(PrimingRequest.getZombieRequest(), PrimingRequest.getZombieResponse());
        verify(response).status(CREATED_201);
    }

    @Test
    public void handleUsesRequestMethodAsPrimingRequestMethodIfNotPresentInPrimedRequest() throws JsonProcessingException {
        when(request.pathInfo()).thenReturn("path");
        when(request.requestMethod()).thenReturn("POST");
        when(request.headers("zombie")).thenReturn("priming");
        when(zombieRequest.getMethod()).thenReturn(null);
        when(deserializer.deserialize(request, PrimingRequest.class)).thenReturn(PrimingRequest);

        zombieRequestHandler.handle(request, response);

        verify(zombieRequest).setMethod(request.requestMethod());
    }

    @Test
    public void handleUsesRequestPathAsPrimingRequestPathIfNotPresentInPrimedRequest() throws JsonProcessingException {
        when(request.pathInfo()).thenReturn("path");
        when(request.requestMethod()).thenReturn("POST");
        when(request.headers("zombie")).thenReturn("priming");
        when(zombieRequest.getPath()).thenReturn(null);
        when(deserializer.deserialize(request, PrimingRequest.class)).thenReturn(PrimingRequest);

        zombieRequestHandler.handle(request, response);

        verify(zombieRequest).setPath(request.pathInfo());
        verify(response).status(CREATED_201);
        verify(response).header("Content-Type", "application/json");
    }

    @Test
    public void handleReturnsPrimingContextMappingsIfZombieHeaderHasListValue() throws JsonProcessingException {
        when(request.headers("zombie")).thenReturn("list");
        when(primedMappingFactory.create(primingContext)).thenReturn(primedRequests);

        final Object got = zombieRequestHandler.handle(request, response);

        assertThat(got).isEqualTo(primedRequests);

        verify(response).status(OK_200);
        verify(response).header("Content-Type", "application/json");
    }

    @Test
    public void handleClearsPrimingContextAndCallHistoryIfZombieHeaderHasResetValue() throws JsonProcessingException {
        when(request.headers("zombie")).thenReturn("reset");

        assertThat(callHistory).isNotEmpty();

        final Object got = zombieRequestHandler.handle(request, response);

        assertThat(got).isEqualTo("Zombie Reset");
        assertThat(callHistory).isEmpty();

        verify(primingContext).clear();
        verify(response).status(OK_200);
    }

    @Test
    public void handleReturnsCallHistoryIfZombieHeaderHasHistoryValue() throws JsonProcessingException {
        when(request.headers("zombie")).thenReturn("history");

        final Object got = zombieRequestHandler.handle(request, response);

        assertThat(got).isEqualTo(callHistory);

        verify(response).status(OK_200);
        verify(response).header("Content-Type", "application/json");
    }

    @Test
    public void handleThrowsRuntimeExceptionIfZombieHeaderHasUnknownValue() throws JsonProcessingException {
        when(request.headers("zombie")).thenReturn("unknownValue");

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("unknownValue");

        zombieRequestHandler.handle(request, response);
    }
}