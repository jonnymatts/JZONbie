package com.jonnymatts.jzonbie;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Request;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JsonDeserializerTest {

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private Request request;

    @Mock private ObjectMapper objectMapper;

    @Mock private PrimingRequest primingRequest;

    private JsonDeserializer jsonDeserializer;

    @Before
    public void setUp() throws Exception {
        jsonDeserializer = new JsonDeserializer(objectMapper);
    }

    @Test
    public void deserializeReturnsPrimingRequestSuccessfully() throws IOException {
        when(objectMapper.readValue(request.body(), PrimingRequest.class)).thenReturn(primingRequest);

        final PrimingRequest got = jsonDeserializer.deserialize(request, PrimingRequest.class);

        assertThat(got).isEqualTo(primingRequest);
    }

    @Test
    public void deserializeThrowsDeserializationExceptionIfObjectMapperThrowsIOException() throws Exception {
        when(objectMapper.readValue(request.body(), PrimingRequest.class)).thenThrow(new IOException());

        expectedException.expect(DeserializationException.class);
        expectedException.expectMessage("Error deserializing");

        jsonDeserializer.deserialize(request, PrimingRequest.class);
    }
}