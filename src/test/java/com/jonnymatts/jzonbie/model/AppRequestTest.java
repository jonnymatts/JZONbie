package com.jonnymatts.jzonbie.model;

import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.util.AppRequestBuilderUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class AppRequestTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Fixture private String username;

    @Fixture private String password;

    private AppRequest appRequest;

    @Before
    public void setUp() throws Exception {
        appRequest = AppRequestBuilderUtil.getFixturedAppRequest();
    }

    @Test
    public void setBasicAuthAddsAuthorizationHeaderToHeadersWithStringInput() {
        final String authValue = String.format("%s:%s", username, password);
        final String encodedAuthValue = Base64.getEncoder().encodeToString(authValue.getBytes());

        appRequest.setBasicAuth(username, password);

        final Map<String, String> headers = appRequest.getHeaders();

        assertThat(headers).containsEntry("Authorization", "Basic " + encodedAuthValue);
    }

    @Test
    public void setBasicAuthAddsAuthorizationHeaderToHeadersWithMapInput() {
        final String authValue = String.format("%s:%s", username, password);
        final String encodedAuthValue = Base64.getEncoder().encodeToString(authValue.getBytes());

        appRequest.setBasicAuth(singletonMap(username, password));

        final Map<String, String> headers = appRequest.getHeaders();

        assertThat(headers).containsEntry("Authorization", "Basic " + encodedAuthValue);
    }

    @Test
    public void matchesReturnsFalseIfPathsDoNotMatch() throws Exception {
        final AppRequest copy = copyAppRequest(appRequest);
        copy.setPath(appRequest.getPath() + "notEqual");

        assertThat(appRequest.matches(copy)).isFalse();
    }

    @Test
    public void matchesReturnsTrueIfPathsMatchRegex() throws Exception {
        appRequest.setPath("path.*");

        final AppRequest copy = copyAppRequest(appRequest);
        copy.setPath(appRequest.getPath() + "extraStuff");

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    public void matchesReturnsFalseIfMethodsDoNotMatch() throws Exception {
        final AppRequest copy = copyAppRequest(appRequest);
        copy.setMethod(appRequest.getMethod() + "notEqual");

        assertThat(appRequest.matches(copy)).isFalse();
    }

    @Test
    public void matchesReturnsFalseIfQueryParamsDoNotMatch() throws Exception {
        final AppRequest copy = copyAppRequest(appRequest);
        copy.getQueryParams().clear();

        assertThat(appRequest.matches(copy)).isFalse();
    }

    @Test
    public void matchesReturnsTrueIfQueryParamsMatchRegex() throws Exception {
        final Map<String, List<String>> appRequestQueryParams = appRequest.getQueryParams();
        appRequestQueryParams.clear();
        appRequestQueryParams.put("key", asList("val.*", "foo.*"));

        final AppRequest copy = copyAppRequest(appRequest);

        copy.getQueryParams().put("key", asList("value", "foobar"));

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    public void matchesReturnsFalseIfBodiesDoNotMatch() throws Exception {
        final AppRequest copy = copyAppRequest(appRequest);
        copy.getBody().clear();

        assertThat(appRequest.matches(copy)).isFalse();
    }

    @Test
    public void matchesReturnsTrueIfBodyValuesMatchRegex() throws Exception {
        final Map<String, Object> appRequestBody = appRequest.getBody();
        appRequestBody.clear();
        appRequestBody.put("key", "val.*");

        final AppRequest copy = copyAppRequest(appRequest);

        copy.getBody().put("key", "value");

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    public void matchesReturnsFalseIfHeadersOfThatRequestDoesNotContainTheHeadersOfThisRequest() throws Exception {
        final AppRequest copy = copyAppRequest(appRequest);
        appRequest.getHeaders().put("var", "val");

        assertThat(appRequest.matches(copy)).isFalse();
    }

    @Test
    public void matchesReturnsTrueIfHeadersOfThisRequestIsEmptyAndEverythingElseMatches() throws Exception {
        final AppRequest copy = copyAppRequest(appRequest);
        appRequest.getHeaders().clear();

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    public void matchesReturnsTrueIfHeadersOfThatRequestContainsTheHeadersOfThisRequest() throws Exception {
        final AppRequest copy = copyAppRequest(appRequest);
        appRequest.getHeaders().remove(appRequest.getHeaders().keySet().iterator().next());

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    public void matchesReturnsTrueIfHeaderValuesMatchRegex() throws Exception {
        final Map<String, String> appRequestHeaders = appRequest.getHeaders();
        appRequestHeaders.clear();
        appRequestHeaders.put("key", "val.*");

        final AppRequest copy = copyAppRequest(appRequest);

        copy.getHeaders().put("key", "value");

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    public void matchesReturnsTrueIfEveryFieldMatches() throws Exception {
        final AppRequest copy = copyAppRequest(appRequest);

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    public void builderCanConstructInstances() {
        final AppRequest request = AppRequest.builder("GET", "/.*")
                .withBody(appRequest.getBody())
                .withBasicAuth(username, password)
                .withHeader("header-name", "header-value")
                .withQueryParam("param-name", "param-value")
                .build();

        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/.*");
        assertThat(request.getQueryParams()).contains(entry("param-name", singletonList("param-value")));
        assertThat(request.getBody()).isEqualTo(appRequest.getBody());
        assertThat(request.getHeaders()).contains(entry("header-name", "header-value"));
        assertThat(request.getHeaders()).containsKey("Authorization");
    }

    private AppRequest copyAppRequest(AppRequest appRequest) {
        final AppRequest copy = new AppRequest();
        copy.setPath(appRequest.getPath());
        copy.setMethod(appRequest.getMethod());
        copy.setQueryParams(new HashMap<>(appRequest.getQueryParams()));
        copy.setHeaders(new HashMap<>(appRequest.getHeaders()));
        copy.setBody(new HashMap<>(appRequest.getBody()));
        return copy;
    }
}