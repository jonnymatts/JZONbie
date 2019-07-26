package com.jonnymatts.jzonbie.jackson;

import com.jonnymatts.jzonbie.Request;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DeserializerTest {

    private static final String ZOMBIE_PRIMING = "{\n" +
            "  \"request\" : {\n" +
            "    \"path\" : \"/\",\n" +
            "    \"method\" : \"GET\"\n" +
            "  },\n" +
            "  \"response\" : {\n" +
            "      \"statusCode\" : 200\n" +
            "  }\n" +
            "}";

    @Mock private Request request;

    private Deserializer deserializer;

    @BeforeEach
    void setUp() throws Exception {
        deserializer = new Deserializer();

        lenient().when(request.getBody()).thenReturn(ZOMBIE_PRIMING);
    }

    @Test
    void deserializeRequestReturnsPrimingRequestSuccessfully() throws IOException {
        final ZombiePriming got = deserializer.deserialize(request, ZombiePriming.class);

        assertThat(got).isEqualTo(
                new ZombiePriming(
                        get("/").build(),
                        ok().build()
                )
        );
    }

    @Test
    void deserializeStringReturnsPrimingRequestSuccessfully() throws IOException {
        final ZombiePriming got = deserializer.deserialize(ZOMBIE_PRIMING, ZombiePriming.class);

        assertThat(got).isEqualTo(
                new ZombiePriming(
                        get("/").build(),
                        ok().build()
                )
        );
    }

    @Test
    void deserializeThrowsDeserializationExceptionIfDeserializationFails() throws Exception {
        assertThatThrownBy(() -> deserializer.deserialize("[{}]", ZombiePriming.class))
                .isInstanceOf(DeserializationException.class)
                .hasMessageContaining("Error deserializing");
    }

    @Test
    void deserializeReturnsEmptyMapIfStringIsEmpty() {
        final Map<String, Object> got = deserializer.deserialize("");

        assertThat(got).isNull();
    }

    @Test
    void deserializeReturnsEmptyMapIfStringIsNull() {
        final Map<String, Object> got = deserializer.deserialize(null);

        assertThat(got).isNull();
    }
}