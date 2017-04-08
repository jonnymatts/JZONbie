package com.jonnymatts.jzonbie.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.requests.Request;
import com.jonnymatts.jzonbie.util.Deserializer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jonnymatts.jzonbie.model.Cloner.cloneRequest;
import static com.jonnymatts.jzonbie.model.content.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.model.content.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.model.content.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.model.content.StringBodyContent.stringBody;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppRequestFactoryTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Mock private Deserializer deserializer;
    @Mock private Request request;

    @Fixture private AppRequest appRequest;
    @Fixture private String path;
    @Fixture private String requestMethod;
    @Fixture private String requestBody;

    private final Map<String, Object> bodyMap = singletonMap("var", "val");
    private final Map<String, String> headers = singletonMap("hVar", "hVal");
    private final Map<String, List<String>> queryParams = singletonMap("qVar", asList("qVal1", "qVal2"));

    private HashMap<String, Object> expectedMap;

    private AppRequestFactory appRequestFactory;

    @Before
    public void setUp() throws Exception {
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
    public void createForRequest() throws Exception {
        appRequest.setBody(objectBody(bodyMap));

        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getBody()).thenReturn(requestBody);

        when(deserializer.deserialize(requestBody)).thenReturn(bodyMap);
        when(deserializer.deserialize(expectedMap, AppRequest.class)).thenReturn(cloneRequest(appRequest));

        final AppRequest got = appRequestFactory.create(request);

        assertThat(got).isEqualTo(appRequest);
    }

    @Test
    public void createReturnsNullQueryParamsIfQueryParamsIsEmpty() throws Exception {
        appRequest.setBody(objectBody(bodyMap));
        expectedMap.put("queryParams", null);

        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getQueryParams()).thenReturn(emptyMap());
        when(request.getBody()).thenReturn(requestBody);

        when(deserializer.deserialize(requestBody)).thenReturn(bodyMap);
        when(deserializer.deserialize(expectedMap, AppRequest.class)).thenReturn(cloneRequest(appRequest));

        final AppRequest got = appRequestFactory.create(request);

        assertThat(got).isEqualTo(appRequest);
    }

    @Test
    public void createReturnsNullQueryParamsIfQueryParamsIsnull() throws Exception {
        appRequest.setBody(objectBody(bodyMap));
        expectedMap.put("queryParams", null);

        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getQueryParams()).thenReturn(null);
        when(request.getBody()).thenReturn(requestBody);

        when(deserializer.deserialize(requestBody)).thenReturn(bodyMap);
        when(deserializer.deserialize(expectedMap, AppRequest.class)).thenReturn(cloneRequest(appRequest));

        final AppRequest got = appRequestFactory.create(request);

        assertThat(got).isEqualTo(appRequest);
    }

    @Test
    public void createReturnsNullBodyIfBodyIsNull() throws Exception {
        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getBody()).thenReturn(null);

        when(deserializer.deserialize(expectedMap, AppRequest.class)).thenReturn(cloneRequest(appRequest));

        final AppRequest got = appRequestFactory.create(request);

        assertThat(got).isEqualTo(appRequest);
    }

    @Test
    public void createReturnsNullBodyIfBodyIsEmptyString() throws Exception {
        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getBody()).thenReturn("");

        when(deserializer.deserialize(expectedMap, AppRequest.class)).thenReturn(cloneRequest(appRequest));

        final AppRequest got = appRequestFactory.create(request);

        assertThat(got).isEqualTo(appRequest);
    }

    @Test
    public void createReturnsLiteralBodyContentBodyIfBodyIsALiteral() throws Exception {
        final String bodyString = "<jzonbie>message</jzonbie>";
        appRequest.setBody(literalBody(bodyString));

        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getBody()).thenReturn(bodyString);

        when(deserializer.deserialize(expectedMap, AppRequest.class)).thenReturn(cloneRequest(appRequest));

        final AppRequest got = appRequestFactory.create(request);

        assertThat(got).isEqualTo(appRequest);
    }

    @Test
    public void createReturnsListBodyContentBodyIfBodyIsAList() throws Exception {
        final String bodyString = "[\"val1\", \"val2\"]";
        final List<String> bodyList = asList("val1", "val2");
        appRequest.setBody(arrayBody(bodyList));

        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getBody()).thenReturn(bodyString);

        when(deserializer.deserialize(eq(bodyString), any(TypeReference.class))).thenReturn(bodyList);

        when(deserializer.deserialize(expectedMap, AppRequest.class)).thenReturn(cloneRequest(appRequest));

        final AppRequest got = appRequestFactory.create(request);

        assertThat(got).isEqualTo(appRequest);
    }

    @Test
    public void createReturnsJsonStringBodyContentBodyIfBodyIsAString() throws Exception {
        final String bodyString = "\"jsonString\"";
        appRequest.setBody(stringBody("jsonString"));

        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getQueryParams()).thenReturn(queryParams);
        when(request.getBody()).thenReturn(bodyString);

        when(deserializer.deserialize(expectedMap, AppRequest.class)).thenReturn(cloneRequest(appRequest));

        final AppRequest got = appRequestFactory.create(request);

        assertThat(got).isEqualTo(appRequest);
    }
}