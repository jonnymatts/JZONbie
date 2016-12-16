package com.jonnymatts.jzonbie.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Request;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeserializerTest {

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private Request request;

    @Mock private ObjectMapper objectMapper;

    @Mock private ZombiePriming ZombiePriming;

    @Mock private HttpResponse httpResponse;

    private Deserializer deserializer;

    @Before
    public void setUp() throws Exception {
        deserializer = new Deserializer(objectMapper);
    }

    @Test
    public void deserializeReturnsPrimingRequestSuccessfully() throws IOException {
        when(objectMapper.readValue(request.body(), ZombiePriming.class)).thenReturn(ZombiePriming);

        final ZombiePriming got = deserializer.deserialize(request, ZombiePriming.class);

        assertThat(got).isEqualTo(ZombiePriming);
    }

    @Test
    public void deserializeThrowsDeserializationExceptionIfObjectMapperThrowsIOException() throws Exception {
        when(objectMapper.readValue(request.body(), ZombiePriming.class)).thenThrow(new IOException());

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
        when(objectMapper.readValue(request.body(), ZombiePriming.class)).thenReturn(ZombiePriming);

        final ZombiePriming got = deserializer.deserialize(httpResponse, ZombiePriming.class);

        assertThat(got).isEqualTo(ZombiePriming);
    }
}