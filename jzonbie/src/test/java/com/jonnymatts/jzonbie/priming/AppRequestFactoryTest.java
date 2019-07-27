package com.jonnymatts.jzonbie.priming;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flextrade.jfixture.JFixture;
import com.jonnymatts.jzonbie.Request;
import com.jonnymatts.jzonbie.jackson.Deserializer;
import com.jonnymatts.jzonbie.requests.AppRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jonnymatts.jzonbie.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.body.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppRequestFactoryTest {

    private static final JFixture FIXTURE = new JFixture();

    @Mock private Deserializer deserializer;
    @Mock private Request request;

    private String path = FIXTURE.create(String.class);
    private String requestMethod = FIXTURE.create(String.class);
    private String requestBody = FIXTURE.create(String.class);

    private final Map<String, Object> bodyMap = singletonMap("var", "val");
    private final Map<String, String> headers = singletonMap("hVar", "hVal");
    private final Map<String, List<String>> queryParams = singletonMap("qVar", asList("qVal1", "qVal2"));
    private final AppRequest appRequest = get("/");

    private HashMap<String, Object> expectedMap;

    private AppRequestFactory appRequestFactory;

    @BeforeEach
    void setUp() throws Exception {
        expectedMap = new HashMap<String, Object>() {{
            put("path", path);
            put("method", requestMethod);
            put("headers", headers);
            put("queryParams", queryParams);
        }};

        requestBody = "{" + requestBody + "}";

        appRequestFactory = new AppRequestFactory(deserializer);
    }

    @Test
    void createForRequest() throws Exception {
        appRequest.setBody(objectBody(bodyMap));

        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getBody()).thenReturn(requestBody);

        when(deserializer.deserialize(requestBody)).thenReturn(bodyMap);
        when(deserializer.deserialize(expectedMap, AppRequest.class)).thenReturn(new AppRequest(appRequest));

        final AppRequest got = appRequestFactory.create(request);

        assertThat(got).isEqualTo(appRequest);
    }

    @Test
    void createReturnsNullBodyIfBodyIsNull() throws Exception {
        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getBody()).thenReturn(null);

        when(deserializer.deserialize(expectedMap, AppRequest.class)).thenReturn(new AppRequest(appRequest));

        final AppRequest got = appRequestFactory.create(request);

        assertThat(got).isEqualTo(appRequest);
    }

    @Test
    void createReturnsNullBodyIfBodyIsEmptyString() throws Exception {
        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getBody()).thenReturn("");

        when(deserializer.deserialize(expectedMap, AppRequest.class)).thenReturn(new AppRequest(appRequest));

        final AppRequest got = appRequestFactory.create(request);

        assertThat(got).isEqualTo(appRequest);
    }

    @Test
    void createReturnsLiteralBodyContentBodyIfBodyIsALiteral() throws Exception {
        final String bodyString = "<jzonbie>message</jzonbie>";
        appRequest.setBody(literalBody(bodyString));

        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getBody()).thenReturn(bodyString);

        when(deserializer.deserialize(expectedMap, AppRequest.class)).thenReturn(new AppRequest(appRequest));

        final AppRequest got = appRequestFactory.create(request);

        assertThat(got).isEqualTo(appRequest);
    }

    @Test
    void createReturnsListBodyContentBodyIfBodyIsAList() throws Exception {
        final String bodyString = "[\"val1\", \"val2\"]";
        final List<String> bodyList = asList("val1", "val2");
        appRequest.setBody(arrayBody(bodyList));

        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getBody()).thenReturn(bodyString);

        when(deserializer.deserialize(eq(bodyString), any(TypeReference.class))).thenReturn(bodyList);

        when(deserializer.deserialize(expectedMap, AppRequest.class)).thenReturn(new AppRequest(appRequest));

        final AppRequest got = appRequestFactory.create(request);

        assertThat(got).isEqualTo(appRequest);
    }

    @Test
    void createReturnsJsonStringBodyContentBodyIfBodyIsAString() throws Exception {
        final String bodyString = "\"jsonString\"";
        appRequest.setBody(stringBody("jsonString"));

        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getBody()).thenReturn(bodyString);

        when(deserializer.deserialize(expectedMap, AppRequest.class)).thenReturn(new AppRequest(appRequest));

        final AppRequest got = appRequestFactory.create(request);

        assertThat(got).isEqualTo(appRequest);
    }
}