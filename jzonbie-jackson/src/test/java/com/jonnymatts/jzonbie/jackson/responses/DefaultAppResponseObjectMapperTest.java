package com.jonnymatts.jzonbie.jackson.responses;

import com.jonnymatts.jzonbie.jackson.JzonbieObjectMapper;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DynamicDefaultAppResponse;
import com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse;
import org.junit.jupiter.api.Test;

import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse.dynamicDefault;
import static com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse.staticDefault;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultAppResponseObjectMapperTest {

    private final static JzonbieObjectMapper JZONBIE_OBJECT_MAPPER = new JzonbieObjectMapper();

    @Test
    void staticDefaultCanBeSerializedAndDeserialized() throws Exception {
        final StaticDefaultAppResponse defaultResponse = staticDefault(ok());

        final String string = JZONBIE_OBJECT_MAPPER.writeValueAsString(defaultResponse);
        final DefaultAppResponse got = JZONBIE_OBJECT_MAPPER.readValue(string, DefaultAppResponse.class);

        assertThat(got).isEqualTo(defaultResponse);
    }

    @Test
    void dynamicDefaultCannotBeSerialized() throws Exception {
        final DynamicDefaultAppResponse defaultResponse = dynamicDefault(AppResponse::ok);

        assertThatThrownBy(() -> JZONBIE_OBJECT_MAPPER.writeValueAsString(defaultResponse))
            .hasRootCauseInstanceOf(UnsupportedOperationException.class);
    }
}