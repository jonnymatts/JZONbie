package com.jonnymatts.jzonbie.jackson.body;

import com.jonnymatts.jzonbie.body.BodyContent;
import com.jonnymatts.jzonbie.jackson.JzonbieObjectMapper;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static com.jonnymatts.jzonbie.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.body.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class BodyContentObjectMapperTest {

    private final static JzonbieObjectMapper JZONBIE_OBJECT_MAPPER = new JzonbieObjectMapper();

    private static final AppRequest APP_REQUEST_BUILDER = get("/");
    private static final AppResponse APP_RESPONSE_BUILDER = ok();

    static Stream<BodyContent<?>> bodies() {
        return Stream.of(
                objectBody(singletonMap("key", "val")),
                arrayBody(singletonList("val")),
                stringBody("string"),
                literalBody("literal"),
                null
        );
    }

    @ParameterizedTest
    @MethodSource("bodies")
    void appRequestsCanBeSerializedAndDeserialized(BodyContent<?> bodyContent) throws IOException {
        System.out.println("Running test: AppResponse - " + (bodyContent == null ? "null" : bodyContent.getType()));
        final AppRequest request = APP_REQUEST_BUILDER
                .withBody(bodyContent);

        final String string = JZONBIE_OBJECT_MAPPER.writeValueAsString(request);
        final AppRequest got = JZONBIE_OBJECT_MAPPER.readValue(string, AppRequest.class);

        assertThat(got).isEqualTo(request);
    }

    @ParameterizedTest
    @MethodSource("bodies")
    void appResponsesCanBeSerializedAndDeserialized(BodyContent<?> bodyContent) throws IOException {
        System.out.println("Running test: AppRequest - " + (bodyContent == null ? "null" : bodyContent.getType()));
        final AppResponse response = APP_RESPONSE_BUILDER
                .withBody(bodyContent);

        final String string = JZONBIE_OBJECT_MAPPER.writeValueAsString(response);
        final AppResponse got = JZONBIE_OBJECT_MAPPER.readValue(string, AppResponse.class);

        assertThat(got).isEqualTo(response);
    }
}