package com.jonnymatts.jzonbie.model;

import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class AppRequestTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Fixture private String username;

    @Fixture private String password;

    @Fixture private AppRequest appRequest;

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
    public void matchesReturnsFalseIfBodiesDoNotMatch() throws Exception {
        final AppRequest copy = copyAppRequest(appRequest);
        copy.getBody().clear();

        assertThat(appRequest.matches(copy)).isFalse();
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
    public void matchesReturnsTrueIfEveryFieldMatches() throws Exception {
        final AppRequest copy = copyAppRequest(appRequest);

        assertThat(appRequest.matches(copy)).isTrue();
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