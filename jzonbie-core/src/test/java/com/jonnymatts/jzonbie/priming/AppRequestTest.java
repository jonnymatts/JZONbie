package com.jonnymatts.jzonbie.priming;

import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Theories.class)
public class AppRequestTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Fixture private String username;

    @Fixture private String password;

    private AppRequest appRequest;

    @Before
    public void setUp() throws Exception {
        appRequest = AppRequestBuilderUtil.getFixturedAppRequest();
    }

    @Theory
    public void setBasicAuthAddsAuthorizationHeaderToHeadersWithStringInput() {
        final String authValue = String.format("%s:%s", username, password);
        final String encodedAuthValue = Base64.getEncoder().encodeToString(authValue.getBytes());

        appRequest.setBasicAuth(username, password);

        final Map<String, String> headers = appRequest.getHeaders();

        assertThat(headers).containsEntry("Authorization", "Basic " + encodedAuthValue);
    }

    @Theory
    public void setBasicAuthAddsAuthorizationHeaderToHeadersWithMapInput() {
        final String authValue = String.format("%s:%s", username, password);
        final String encodedAuthValue = Base64.getEncoder().encodeToString(authValue.getBytes());

        appRequest.setBasicAuth(singletonMap(username, password));

        final Map<String, String> headers = appRequest.getHeaders();

        assertThat(headers).containsEntry("Authorization", "Basic " + encodedAuthValue);
    }

    @Theory
    public void matchesReturnsFalseIfPathsDoNotMatch() throws Exception {
        final AppRequest copy = Cloner.cloneRequest(appRequest);
        copy.setPath(appRequest.getPath() + "notEqual");

        assertThat(appRequest.matches(copy)).isFalse();
    }

    @Theory
    public void matchesReturnsTrueIfPathsMatchRegex() throws Exception {
        appRequest.setPath("path.*");

        final AppRequest copy = Cloner.cloneRequest(appRequest);
        copy.setPath(appRequest.getPath() + "extraStuff");

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Theory
    public void matchesReturnsFalseIfMethodsDoNotMatch() throws Exception {
        final AppRequest copy = Cloner.cloneRequest(appRequest);
        copy.setMethod(appRequest.getMethod() + "notEqual");

        assertThat(appRequest.matches(copy)).isFalse();
    }

    @Theory
    public void matchesReturnsFalseIfHeadersOfThatRequestDoesNotContainTheHeadersOfThisRequest() throws Exception {
        final AppRequest copy = Cloner.cloneRequest(appRequest);
        appRequest.getHeaders().put("var", "val");

        assertThat(appRequest.matches(copy)).isFalse();
    }

    @Theory
    public void matchesReturnsTrueIfHeadersOfThisRequestIsEmptyAndEverythingElseMatches() throws Exception {
        final AppRequest copy = Cloner.cloneRequest(appRequest);
        appRequest.getHeaders().clear();

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Theory
    public void matchesReturnsTrueIfHeadersOfThatRequestContainsTheHeadersOfThisRequest() throws Exception {
        final AppRequest copy = Cloner.cloneRequest(appRequest);
        appRequest.getHeaders().remove(appRequest.getHeaders().keySet().iterator().next());

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Theory
    public void matchesReturnsTrueIfHeaderValuesMatchRegex() throws Exception {
        final Map<String, String> appRequestHeaders = appRequest.getHeaders();
        appRequestHeaders.clear();
        appRequestHeaders.put("key", "val.*");

        final AppRequest copy = Cloner.cloneRequest(appRequest);

        copy.getHeaders().put("key", "value");

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Theory
    public void matchesReturnsFalseIfQueryParamsOfThatRequestDoesNotContainTheQueryParamsOfThisRequest() throws Exception {
        final AppRequest copy = Cloner.cloneRequest(appRequest);
        appRequest.getQueryParams().put("var", singletonList("val"));

        assertThat(appRequest.matches(copy)).isFalse();
    }

    @Theory
    public void matchesReturnsTrueIfQueryParamsOfThisRequestIsEmptyAndEverythingElseMatches() throws Exception {
        final AppRequest copy = Cloner.cloneRequest(appRequest);
        appRequest.getQueryParams().clear();

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Theory
    public void matchesReturnsTrueIfQueryParamsOfThatRequestContainsTheQueryParamsOfThisRequest() throws Exception {
        final AppRequest copy = Cloner.cloneRequest(appRequest);
        appRequest.getQueryParams().remove(appRequest.getQueryParams().keySet().iterator().next());

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Theory
    public void matchesReturnsTrueIfQueryParamsValuesMatchRegex() throws Exception {
        final Map<String, List<String>> appRequestParams = appRequest.getQueryParams();
        appRequestParams.clear();
        appRequestParams.put("key", singletonList("val.*"));

        final AppRequest copy = Cloner.cloneRequest(appRequest);

        copy.getQueryParams().put("key", singletonList("value"));

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Theory
    public void matchesReturnsTrueIfEveryFieldMatches() throws Exception {
        final AppRequest copy = Cloner.cloneRequest(appRequest);

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Theory
    public void matchesReturnsTrueIfBodyOfThisRequestIsNullAndBodyOfThatRequestIsNull() throws Exception {
        appRequest.setBody(null);

        final AppRequest copy = Cloner.cloneRequest(appRequest);

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Theory
    public void matchesReturnsTrueIfBodyOfThisRequestIsNullAndBodyOfThatRequestIsNotNull() throws Exception {
        final AppRequest copy = Cloner.cloneRequest(appRequest);
        appRequest.setBody(null);

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @DataPoints("staticBuilders")
    public static StaticBuilderData[] staticBuilders = new StaticBuilderData[]{
            new StaticBuilderData("GET", AppRequest::get),
            new StaticBuilderData("POST", AppRequest::post),
            new StaticBuilderData("HEAD", AppRequest::head),
            new StaticBuilderData("PUT", AppRequest::put),
            new StaticBuilderData("OPTIONS", AppRequest::options),
            new StaticBuilderData("DELETE", AppRequest::delete)
    };

    @Theory
    public void staticBuildersCreatesRequest(@FromDataPoints("staticBuilders") StaticBuilderData data) {
        System.out.println("Testing static builder: " + data.method);

        final AppRequest request = data.builderFunc.apply("/.*").build();

        assertThat(request.getMethod()).isEqualTo(data.method);
        assertThat(request.getPath()).isEqualTo("/.*");
    }

    private static class StaticBuilderData {
        private final String method;
        private final Function<String, AppRequestBuilder> builderFunc;

        private StaticBuilderData(String method, Function<String, AppRequestBuilder> builderFunc) {
            this.method = method;
            this.builderFunc = builderFunc;
        }
    }
}