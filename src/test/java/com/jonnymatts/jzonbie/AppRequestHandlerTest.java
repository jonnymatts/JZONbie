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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppRequestHandlerTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private Multimap<PrimingKey, PrimedResponse> primingContext;

    @Mock private PrimingKeyFactory primingKeyFactory;

    @Mock private ObjectMapper objectMapper;

    @Mock private Request request;

    @Mock private Response response;

    @Fixture private PrimingRequest primingRequest;

    @Fixture private PrimingKey primingKey;

    @Fixture private String path;

    @Fixture private String responseString;

    private AppRequestHandler appRequestHandler;

    @Before
    public void setUp() throws Exception {
        appRequestHandler = new AppRequestHandler(primingContext, primingKeyFactory, objectMapper);
    }

    @Test
    public void handleReturnsPrimedResponseIfPrimingKeyExistsInPrimingContext() throws JsonProcessingException {
        final PrimedResponse primedResponse = primingRequest.getPrimedResponse();

        when(primingKeyFactory.create(request)).thenReturn(primingKey);
        when(primingContext.get(primingKey))
                .thenReturn(singletonList(primedResponse));
        when(objectMapper.writeValueAsString(primedResponse.getBody())).thenReturn(responseString);

        final String got = appRequestHandler.handle(request, response);

        assertThat(got).isEqualTo(responseString);

        verify(response).status(primedResponse.getStatusCode());
        primedResponse.getHeaders().entrySet()
                .forEach(entry -> verify(response).header(entry.getKey(), entry.getValue()));
        verify(primingContext).remove(primingKey, primedResponse);
    }
}