package com.jonnymatts.jzonbie.jackson.body;

import com.jonnymatts.jzonbie.jackson.JzonbieObjectMapper;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.requests.AppRequestBuilder;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.AppResponseBuilder;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.jonnymatts.jzonbie.jackson.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.jackson.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.jackson.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.jackson.body.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Theories.class)
public class BodyContentObjectMapperTest {

    private final static JzonbieObjectMapper JZONBIE_OBJECT_MAPPER = new JzonbieObjectMapper();

    private static final AppRequestBuilder APP_REQUEST_BUILDER = get("/");
    private static final AppResponseBuilder APP_RESPONSE_BUILDER = ok();

    @DataPoints("bodies") public static BodyContent<?>[] bodies = new BodyContent[]{
            objectBody(singletonMap("key", "val")),
            arrayBody(singletonList("val")),
            stringBody("string"),
            literalBody("literal"),
            null
    };

    @Theory
    public void appRequestsCanBeSerializedAndDeserialized(@FromDataPoints("bodies") BodyContent<?> bodyContent) throws IOException {
        System.out.println("Running test: AppResponse - " + (bodyContent == null ? "null" : bodyContent.getType()));
        final AppRequest request = APP_REQUEST_BUILDER
                .withBody(bodyContent)
                .build();

        final String string = JZONBIE_OBJECT_MAPPER.writeValueAsString(request);
        final AppRequest got = JZONBIE_OBJECT_MAPPER.readValue(string, AppRequest.class);

        assertThat(got).isEqualTo(request);
    }

    @Theory
    public void appResponsesCanBeSerializedAndDeserialized(@FromDataPoints("bodies") BodyContent<?> bodyContent) throws IOException {
        System.out.println("Running test: AppRequest - " + (bodyContent == null ? "null" : bodyContent.getType()));
        final AppResponse response = APP_RESPONSE_BUILDER
                .withBody(bodyContent)
                .build();

        final String string = JZONBIE_OBJECT_MAPPER.writeValueAsString(response);
        final AppResponse got = JZONBIE_OBJECT_MAPPER.readValue(string, AppResponse.class);

        assertThat(got).isEqualTo(response);
    }
}