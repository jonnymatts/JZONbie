package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.google.common.collect.Multimap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Request;
import spark.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ZombieRequestHandlerTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private Multimap<PrimingKey, PrimedResponse> primingContext;

    @Mock private JsonDeserializer jsonDeserializer;

    @Mock private ObjectMapper objectMapper;

    @Mock private Request request;

    @Mock private Response response;

    @Mock private PrimingRequest primingRequest;

    @Mock private PrimedRequest primedRequest;

    @Mock private PrimedResponse primedResponse;

    @Fixture private String primingRequestString;

    private ZombieRequestHandler zombieRequestHandler;

    @Before
    public void setUp() throws Exception {
        zombieRequestHandler = new ZombieRequestHandler(primingContext, jsonDeserializer, objectMapper);

        when(primingRequest.getPrimedRequest()).thenReturn(primedRequest);
        when(primingRequest.getPrimedResponse()).thenReturn(primedResponse);
    }

    @Test
    public void handleAddsRequestToPrimingContextIfZombieHeaderHasPrimingValue() throws JsonProcessingException {
        when(request.pathInfo()).thenReturn("path");
        when(request.headers("zombie")).thenReturn("priming");
        when(jsonDeserializer.deserialize(request, PrimingRequest.class)).thenReturn(primingRequest);
        when(objectMapper.writeValueAsString(primingRequest)).thenReturn(primingRequestString);

        final String got = zombieRequestHandler.handle(request, response);

        assertThat(got).isEqualTo(primingRequestString);

        verify(primingContext).put(new PrimingKey(request.pathInfo(), primingRequest.getPrimedRequest()), primingRequest.getPrimedResponse());
        verify(response).status(CREATED_201);
    }

    @Test
    public void handleUsesRequestMethodAsPrimingRequestMethodIfNotPresentInPrimedRequest() throws JsonProcessingException {
        when(request.pathInfo()).thenReturn("path");
        when(request.requestMethod()).thenReturn("POST");
        when(request.headers("zombie")).thenReturn("priming");
        when(primedRequest.getMethod()).thenReturn(null);
        when(jsonDeserializer.deserialize(request, PrimingRequest.class)).thenReturn(primingRequest);
        when(objectMapper.writeValueAsString(primingRequest)).thenReturn(primingRequestString);

        zombieRequestHandler.handle(request, response);

        verify(primedRequest).setMethod(request.requestMethod());
    }

    @Test
    public void handleThrowsRuntimeExceptionIfZombieHeaderHasUnknownValue() throws JsonProcessingException {
        when(request.headers("zombie")).thenReturn("unknownValue");

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("unknownValue");

        zombieRequestHandler.handle(request, response);
    }
}