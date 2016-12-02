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

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppRequestHandlerTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private Multimap<PrimedRequest, PrimedResponse> primingContext;

    @Mock private List<PrimingRequest> callHistory;

    @Mock private PrimedRequestFactory primedRequestFactory;

    @Mock private ObjectMapper objectMapper;

    @Mock private Request request;

    @Mock private Response response;

    @Fixture private PrimingRequest primingRequest;

    @Fixture private String path;

    @Fixture private String responseString;

    private AppRequestHandler appRequestHandler;

    private PrimedRequest primedRequest;

    private PrimedResponse primedResponse;

    @Before
    public void setUp() throws Exception {
        appRequestHandler = new AppRequestHandler(primingContext, callHistory, primedRequestFactory, objectMapper);

        primedRequest = primingRequest.getPrimedRequest();
        primedResponse = primingRequest.getPrimedResponse();

        when(primedRequestFactory.create(request)).thenReturn(primedRequest);
        when(primingContext.get(primedRequest))
                .thenReturn(singletonList(primedResponse));
        when(objectMapper.writeValueAsString(primedResponse.getBody())).thenReturn(responseString);
    }

    @Test
    public void handleReturnsPrimedResponseIfPrimingKeyExistsInPrimingContext() throws JsonProcessingException {
        final String got = appRequestHandler.handle(request, response);

        assertThat(got).isEqualTo(responseString);

        verify(response).status(primedResponse.getStatusCode());
        primedResponse.getHeaders().entrySet()
                .forEach(entry -> verify(response).header(entry.getKey(), entry.getValue()));
        verify(primingContext).remove(primedRequest, primedResponse);
    }

    @Test
    public void handleDoesNotAddHeadersToResponseIfPrimedResponseDoesNotHaveHeaders() throws JsonProcessingException {
        primedResponse.setHeaders(null);

        appRequestHandler.handle(request, response);

        verify(response).status(primedResponse.getStatusCode());
        verifyNoMoreInteractions(response);
    }

    @Test
    public void handleAddsPrimingRequestToCallHistory() throws JsonProcessingException {
        appRequestHandler.handle(request, response);

        verify(callHistory).add(primingRequest);
    }
}