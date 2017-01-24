package com.jonnymatts.jzonbie.requests;

import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SparkRequestTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Mock(answer = RETURNS_DEEP_STUBS) private Request request;

    @Fixture private String path;
    @Fixture private String method;
    @Fixture private Set<String> headerNames;
    @Fixture private String body;

    private SparkRequest sparkRequest;

    @Before
    public void setUp() throws Exception {
        when(request.pathInfo()).thenReturn(path);
        when(request.requestMethod()).thenReturn(method);
        when(request.headers()).thenReturn(headerNames);
        headerNames.forEach(name -> when(request.headers(name)).thenReturn(name.toUpperCase()));
        when(request.body()).thenReturn(body);
        when(request.queryMap().toMap()).thenReturn(new HashMap<String, String[]>(){{
            put("qVar1", new String[]{"qVal1", "qVal2"});
            put("qVar2", new String[]{"qVal1"});
            put("qVar3", new String[]{});
        }});

        sparkRequest = new SparkRequest(request);
    }

    @Test
    public void getPathReturnsTheCorrectPath() throws Exception {
        final String got = sparkRequest.getPath();

        assertThat(got).isEqualTo(path);
    }

    @Test
    public void getMethodReturnsTheCorrectMethod() throws Exception {
        final String got = sparkRequest.getMethod();

        assertThat(got).isEqualTo(method);
    }

    @Test
    public void getHeadersReturnsTheCorrectHeaders() throws Exception {
        final Map<String, String> expectedHeaders = headerNames.stream().collect(toMap(identity(), String::toUpperCase));

        final Map<String, String> got = sparkRequest.getHeaders();

        assertThat(got).isEqualTo(expectedHeaders);
    }

    @Test
    public void getBodyReturnsTheCorrectBody() throws Exception {
        final String got = sparkRequest.getBody();

        assertThat(got).isEqualTo(body);
    }

    @Test
    public void getQueryParamsReturnsTheCorrectQueryParams() throws Exception {
        final Map<String, List<String>> expectedMap = new HashMap<String, List<String>>(){{
            put("qVar1", asList("qVal1", "qVal2"));
            put("qVar2", singletonList("qVal1"));
            put("qVar3", emptyList());
        }};

        final Map<String, List<String>> got = sparkRequest.getQueryParams();

        assertThat(got).isEqualTo(expectedMap);
    }
}