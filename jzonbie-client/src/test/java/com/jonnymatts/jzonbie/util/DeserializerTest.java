package com.jonnymatts.jzonbie.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.requests.Request;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeserializerTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private Request request;

    @Mock private ObjectMapper objectMapper;

    @Mock private ZombiePriming zombiePriming;

    @Mock private HttpResponse httpResponse;

    @Fixture private String responseString;
    @Fixture private String requestBody;

    private Deserializer deserializer;

    @Before
    public void setUp() throws Exception {
        deserializer = new Deserializer(objectMapper);

        when(request.getBody()).thenReturn(requestBody);
    }

    @Test
    public void deserializeReturnsPrimingRequestSuccessfully() throws IOException {
        when(objectMapper.readValue(request.getBody(), ZombiePriming.class)).thenReturn(zombiePriming);

        final ZombiePriming got = deserializer.deserialize(request, ZombiePriming.class);

        assertThat(got).isEqualTo(zombiePriming);
    }

    @Test
    public void deserializeThrowsDeserializationExceptionIfObjectMapperThrowsIOException() throws Exception {
        when(objectMapper.readValue(request.getBody(), ZombiePriming.class)).thenThrow(new IOException());

        expectedException.expect(DeserializationException.class);
        expectedException.expectMessage("Error deserializing");

        deserializer.deserialize(request, ZombiePriming.class);
    }

    @Test
    public void deserializeReturnsEmptyMapIfStringIsEmpty() {
        final Map<String, Object> got = deserializer.deserialize("");

        assertThat(got).isNull();
    }

    @Test
    public void deserializeReturnsEmptyMapIfStringIsNull() {
        final Map<String, Object> got = deserializer.deserialize(null);

        assertThat(got).isNull();
    }

    @Test
    public void deserializeCanDeserializeApacheHttpResponseBodySuccessfully() throws IOException {
        when(httpResponse.getEntity()).thenReturn(new StringEntity(responseString));
        when(objectMapper.readValue(responseString, ZombiePriming.class)).thenReturn(zombiePriming);

        final ZombiePriming got = deserializer.deserialize(httpResponse, ZombiePriming.class);

        assertThat(got).isEqualTo(zombiePriming);
    }
}