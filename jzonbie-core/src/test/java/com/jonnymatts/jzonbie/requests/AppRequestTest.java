package com.jonnymatts.jzonbie.requests;

import com.flextrade.jfixture.JFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.jonnymatts.jzonbie.body.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.util.Cloner.cloneRequest;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class AppRequestTest {

    private static final JFixture FIXTURE = new JFixture();

    private static final String username = FIXTURE.create(String.class);
    private static final String password = FIXTURE.create(String.class);

    private AppRequest appRequest;

    @BeforeEach
    void setUp() throws Exception {
        appRequest = get("/")
                .contentType("application/json")
                .withBody(stringBody("test"))
                .withQueryParam("param", "value")
                .build();
    }

    @Test
    void setBasicAuthAddsAuthorizationHeaderToHeadersWithStringInput() {
        final String authValue = String.format("%s:%s", username, password);
        final String encodedAuthValue = Base64.getEncoder().encodeToString(authValue.getBytes());

        appRequest.setBasicAuth(username, password);

        final Map<String, String> headers = appRequest.getHeaders();

        assertThat(headers).containsEntry("Authorization", "Basic " + encodedAuthValue);
    }

    @Test
    void setBasicAuthAddsAuthorizationHeaderToHeadersWithMapInput() {
        final String authValue = String.format("%s:%s", username, password);
        final String encodedAuthValue = Base64.getEncoder().encodeToString(authValue.getBytes());

        appRequest.setBasicAuth(singletonMap(username, password));

        final Map<String, String> headers = appRequest.getHeaders();

        assertThat(headers).containsEntry("Authorization", "Basic " + encodedAuthValue);
    }

    @Test
    void matchesReturnsFalseIfPathsDoNotMatch() throws Exception {
        final AppRequest copy = cloneRequest(appRequest);
        copy.setPath(appRequest.getPath() + "notEqual");

        assertThat(appRequest.matches(copy)).isFalse();
    }

    @Test
    void matchesReturnsTrueIfPathsMatchRegex() throws Exception {
        appRequest.setPath("path.*");

        final AppRequest copy = cloneRequest(appRequest);
        copy.setPath(appRequest.getPath() + "extraStuff");

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    void matchesReturnsFalseIfMethodsDoNotMatch() throws Exception {
        final AppRequest copy = cloneRequest(appRequest);
        copy.setMethod(appRequest.getMethod() + "notEqual");

        assertThat(appRequest.matches(copy)).isFalse();
    }

    @Test
    void matchesReturnsFalseIfHeadersOfThatRequestDoesNotContainTheHeadersOfThisRequest() throws Exception {
        final AppRequest copy = cloneRequest(appRequest);
        appRequest.getHeaders().put("var", "val");

        assertThat(appRequest.matches(copy)).isFalse();
    }

    @Test
    void matchesReturnsTrueIfHeadersOfThisRequestIsEmptyAndEverythingElseMatches() throws Exception {
        final AppRequest copy = cloneRequest(appRequest);
        appRequest.getHeaders().clear();

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    void matchesReturnsTrueIfHeadersOfThatRequestContainsTheHeadersOfThisRequest() throws Exception {
        final AppRequest copy = cloneRequest(appRequest);
        appRequest.getHeaders().remove(appRequest.getHeaders().keySet().iterator().next());

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    void matchesReturnsTrueIfHeaderValuesMatchRegex() throws Exception {
        final Map<String, String> appRequestHeaders = appRequest.getHeaders();
        appRequestHeaders.clear();
        appRequestHeaders.put("key", "val.*");

        final AppRequest copy = cloneRequest(appRequest);

        copy.getHeaders().put("key", "value");

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    void matchesReturnsFalseIfQueryParamsOfThatRequestDoesNotContainTheQueryParamsOfThisRequest() throws Exception {
        final AppRequest copy = cloneRequest(appRequest);
        appRequest.getQueryParams().put("var", singletonList("val"));

        assertThat(appRequest.matches(copy)).isFalse();
    }

    @Test
    void matchesReturnsTrueIfQueryParamsOfThisRequestIsEmptyAndEverythingElseMatches() throws Exception {
        final AppRequest copy = cloneRequest(appRequest);
        appRequest.getQueryParams().clear();

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    void matchesReturnsTrueIfQueryParamsOfThatRequestContainsTheQueryParamsOfThisRequest() throws Exception {
        final AppRequest copy = cloneRequest(appRequest);
        appRequest.getQueryParams().remove(appRequest.getQueryParams().keySet().iterator().next());

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    void matchesReturnsTrueIfQueryParamsValuesMatchRegex() throws Exception {
        final Map<String, List<String>> appRequestParams = appRequest.getQueryParams();
        appRequestParams.clear();
        appRequestParams.put("key", singletonList("val.*"));

        final AppRequest copy = cloneRequest(appRequest);

        copy.getQueryParams().put("key", singletonList("value"));

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    void matchesReturnsTrueIfEveryFieldMatches() throws Exception {
        final AppRequest copy = cloneRequest(appRequest);

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    void matchesReturnsTrueIfBodyOfThisRequestIsNullAndBodyOfThatRequestIsNull() throws Exception {
        appRequest.setBody(null);

        final AppRequest copy = cloneRequest(appRequest);

        assertThat(appRequest.matches(copy)).isTrue();
    }

    @Test
    void matchesReturnsTrueIfBodyOfThisRequestIsNullAndBodyOfThatRequestIsNotNull() throws Exception {
        final AppRequest copy = cloneRequest(appRequest);
        appRequest.setBody(null);

        assertThat(appRequest.matches(copy)).isTrue();
    }

    static Stream<StaticBuilderData> staticBuilders() {
        return Stream.of(
            new StaticBuilderData("GET", AppRequest::get),
            new StaticBuilderData("POST", AppRequest::post),
            new StaticBuilderData("HEAD", AppRequest::head),
            new StaticBuilderData("PUT", AppRequest::put),
            new StaticBuilderData("OPTIONS", AppRequest::options),
            new StaticBuilderData("DELETE", AppRequest::delete)
        );
    }

    @ParameterizedTest
    @MethodSource("staticBuilders")
    void staticBuildersCreatesRequest(StaticBuilderData data) {
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