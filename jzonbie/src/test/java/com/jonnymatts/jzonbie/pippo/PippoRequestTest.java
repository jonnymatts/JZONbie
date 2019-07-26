package com.jonnymatts.jzonbie.pippo;

import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ro.pippo.core.FileItem;
import ro.pippo.core.ParameterValue;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterators.asEnumeration;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PippoRequestTest {

    @Rule
    public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Mock(answer = RETURNS_DEEP_STUBS) private ro.pippo.core.Request request;
    @Mock private FileItem fileItem;

    @Fixture private String path;
    @Fixture private String method;
    @Fixture private List<String> headerNames;
    @Fixture private String body;

    private PippoRequest pippoRequest;

    @Before
    public void setUp() throws Exception {
        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(method);
        when(request.getHttpServletRequest().getHeaderNames()).thenReturn(asEnumeration(headerNames.iterator()));
        headerNames.forEach(name -> when(request.getHeader(name)).thenReturn(name.toUpperCase()));
        when(request.getBody()).thenReturn(body);
        when(request.getQueryParameters()).thenReturn(new HashMap<String, ParameterValue>(){{
            put("qVar1", new ParameterValue("qVal1", "qVal2"));
            put("qVar2", new ParameterValue("qVal1"));
            put("qVar3", new ParameterValue());
        }});
        when(request.getFile("priming").getInputStream()).thenReturn(new ByteArrayInputStream(body.getBytes()));

        pippoRequest = new PippoRequest(request);
    }

    @Test
    public void getPathReturnsTheCorrectPath() throws Exception {
        final String got = pippoRequest.getPath();

        assertThat(got).isEqualTo(path);
    }

    @Test
    public void getMethodReturnsTheCorrectMethod() throws Exception {
        final String got = pippoRequest.getMethod();

        assertThat(got).isEqualTo(method);
    }

    @Test
    public void getHeadersReturnsTheCorrectHeaders() throws Exception {
        final Map<String, String> expectedHeaders = headerNames.stream().collect(toMap(identity(), String::toUpperCase));

        final Map<String, String> got = pippoRequest.getHeaders();

        assertThat(got).isEqualTo(expectedHeaders);
    }

    @Test
    public void getBodyReturnsTheCorrectBody() throws Exception {
        final String got = pippoRequest.getBody();

        assertThat(got).isEqualTo(body);
    }

    @Test
    public void getQueryParamsReturnsTheCorrectQueryParams() throws Exception {
        final Map<String, List<String>> expectedMap = new HashMap<String, List<String>>(){{
            put("qVar1", asList("qVal1", "qVal2"));
            put("qVar2", singletonList("qVal1"));
            put("qVar3", emptyList());
        }};

        final Map<String, List<String>> got = pippoRequest.getQueryParams();

        assertThat(got).isEqualTo(expectedMap);
    }

    @Test
    public void getPrimingFileContentReturnsContentOfPrimingFile() throws Exception {
        when(request.getContentType()).thenReturn("multipart/form-data");

        final String got = new PippoRequest(request).getPrimingFileContent();

        assertThat(got).isEqualTo(body);
    }

    @Test
    public void getPrimingFileContentReturnsNullIfPrimingFileIsNotPresent() throws Exception {
        when(request.getContentType()).thenReturn("multipart/form-data");

        final String got = new PippoRequest(request).getPrimingFileContent();

        assertThat(got).isEqualTo(body);
    }
}