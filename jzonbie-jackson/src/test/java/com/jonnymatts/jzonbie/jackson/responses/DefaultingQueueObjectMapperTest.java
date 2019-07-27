package com.jonnymatts.jzonbie.jackson.responses;

import com.jonnymatts.jzonbie.jackson.JzonbieObjectMapper;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultingQueue;
import com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse;
import org.junit.jupiter.api.Test;

import static com.jonnymatts.jzonbie.responses.AppResponse.*;
import static com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse.staticDefault;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultingQueueObjectMapperTest {

    private final static JzonbieObjectMapper JZONBIE_OBJECT_MAPPER = new JzonbieObjectMapper();

    @Test
    void defaultingQueueCanBeSerializedAndDeserialized() throws Exception {
        final AppResponse response1 = ok();
        final AppResponse response2 = created();
        final AppResponse response3 = accepted();
        final StaticDefaultAppResponse defaultResponse = staticDefault(notFound());
        final DefaultingQueue queue = new DefaultingQueue();
        queue.setDefault(defaultResponse);
        queue.add(response1);
        queue.add(response2);
        queue.add(response3);

        final String string = JZONBIE_OBJECT_MAPPER.writeValueAsString(queue);
        final DefaultingQueue got = JZONBIE_OBJECT_MAPPER.readValue(string, DefaultingQueue.class);

        assertThat(got).isEqualTo(queue);
    }
}