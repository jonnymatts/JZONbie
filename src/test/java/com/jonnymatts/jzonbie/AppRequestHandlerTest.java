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

    @Mock private PrimedRequestFactory primedRequestFactory;

    @Mock private ObjectMapper objectMapper;

    @Mock private Request request;

    @Mock private Response response;

    @Fixture private PrimingRequest primingRequest;

    @Fixture private PrimedRequest primedRequest;

    @Fixture private String path;

    @Fixture private String responseString;

    private AppRequestHandler appRequestHandler;

    @Before
    public void setUp() throws Exception {
        appRequestHandler = new AppRequestHandler(primingContext, primedRequestFactory, objectMapper);
    }

    @Test
    public void handleReturnsPrimedResponseIfPrimingKeyExistsInPrimingContext() throws JsonProcessingException {
        final PrimedResponse primedResponse = primingRequest.getPrimedResponse();

        when(primedRequestFactory.create(request)).thenReturn(primedRequest);
        when(primingContext.get(primedRequest))
                .thenReturn(singletonList(primedResponse));
        when(objectMapper.writeValueAsString(primedResponse.getBody())).thenReturn(responseString);

        final String got = appRequestHandler.handle(request, response);

        assertThat(got).isEqualTo(responseString);

        verify(response).status(primedResponse.getStatusCode());
        primedResponse.getHeaders().entrySet()
                .forEach(entry -> verify(response).header(entry.getKey(), entry.getValue()));
        verify(primingContext).remove(primedRequest, primedResponse);
    }

    @Test
    public void handleDoesNotAddHeadersToResponseIfPrimedResponseDoesNotHaveHeaders() throws JsonProcessingException {
        final PrimedResponse primedResponse = primingRequest.getPrimedResponse();

        primedResponse.setHeaders(null);

        when(primedRequestFactory.create(request)).thenReturn(primedRequest);
        when(primingContext.get(primedRequest))
                .thenReturn(singletonList(primedResponse));
        when(objectMapper.writeValueAsString(primedResponse.getBody())).thenReturn(responseString);

        final String got = appRequestHandler.handle(request, response);

        assertThat(got).isEqualTo(responseString);

        verify(response).status(primedResponse.getStatusCode());
        verify(primingContext).remove(primedRequest, primedResponse);
        verifyNoMoreInteractions(response);
    }
}