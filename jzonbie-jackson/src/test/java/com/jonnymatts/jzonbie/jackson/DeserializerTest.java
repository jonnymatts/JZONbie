package com.jonnymatts.jzonbie.jackson;

import com.jonnymatts.jzonbie.Request;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Map;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeserializerTest {

    private static final String ZOMBIE_PRIMING = "{\n" +
            "  \"request\" : {\n" +
            "    \"path\" : \"/\",\n" +
            "    \"method\" : \"GET\"\n" +
            "  },\n" +
            "  \"response\" : {\n" +
            "      \"statusCode\" : 200\n" +
            "  }\n" +
            "}";

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock private Request request;

    private Deserializer deserializer;

    @Before
    public void setUp() throws Exception {
        deserializer = new Deserializer();

        when(request.getBody()).thenReturn(ZOMBIE_PRIMING);
    }

    @Test
    public void deserializeRequestReturnsPrimingRequestSuccessfully() throws IOException {
        final ZombiePriming got = deserializer.deserialize(request, ZombiePriming.class);

        assertThat(got).isEqualTo(
                new ZombiePriming(
                        get("/").build(),
                        ok().build()
                )
        );
    }

    @Test
    public void deserializeStringReturnsPrimingRequestSuccessfully() throws IOException {
        final ZombiePriming got = deserializer.deserialize(ZOMBIE_PRIMING, ZombiePriming.class);

        assertThat(got).isEqualTo(
                new ZombiePriming(
                        get("/").build(),
                        ok().build()
                )
        );
    }

    @Test
    public void deserializeThrowsDeserializationExceptionIfDeserializationFails() throws Exception {
        assertThatThrownBy(() -> deserializer.deserialize("[{}]", ZombiePriming.class))
                .isInstanceOf(DeserializationException.class)
                .hasMessageContaining("Error deserializing");
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