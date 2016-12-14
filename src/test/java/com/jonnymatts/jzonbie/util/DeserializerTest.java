package com.jonnymatts.jzonbie.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.model.PrimingRequest;
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

    @Mock private PrimingRequest PrimingRequest;

    private Deserializer deserializer;

    @Before
    public void setUp() throws Exception {
        deserializer = new Deserializer(objectMapper);
    }

    @Test
    public void deserializeReturnsPrimingRequestSuccessfully() throws IOException {
        when(objectMapper.readValue(request.body(), PrimingRequest.class)).thenReturn(PrimingRequest);

        final PrimingRequest got = deserializer.deserialize(request, PrimingRequest.class);

        assertThat(got).isEqualTo(PrimingRequest);
    }

    @Test
    public void deserializeThrowsDeserializationExceptionIfObjectMapperThrowsIOException() throws Exception {
        when(objectMapper.readValue(request.body(), PrimingRequest.class)).thenThrow(new IOException());

        expectedException.expect(DeserializationException.class);
        expectedException.expectMessage("Error deserializing");

        deserializer.deserialize(request, PrimingRequest.class);
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
}