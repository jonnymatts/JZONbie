package com.jonnymatts.jzonbie.pippo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.jknack.handlebars.Handlebars;
import com.google.common.base.Stopwatch;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jonnymatts.jzonbie.JzonbieOptions;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.requests.AppRequestHandler;
import com.jonnymatts.jzonbie.requests.PrimedMappingUploader;
import com.jonnymatts.jzonbie.requests.ZombieRequestHandler;
import com.jonnymatts.jzonbie.response.CurrentPrimingFileResponseFactory;
import com.jonnymatts.jzonbie.util.AppRequestBuilderUtil;
import com.jonnymatts.jzonbie.util.AppResponseBuilderUtil;
import com.jonnymatts.jzonbie.util.Deserializer;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import ro.pippo.core.util.IoUtils;
import ro.pippo.test.PippoRule;
import ro.pippo.test.PippoTest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.jonnymatts.jzonbie.model.AppResponse.*;
import static com.jonnymatts.jzonbie.model.TemplatedAppResponse.templated;
import static com.jonnymatts.jzonbie.model.content.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.model.content.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.model.content.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.response.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

@RunWith(MockitoJUnitRunner.class)
public class PippoApplicationTest extends PippoTest {

    private static PrimingContext primingContext = new PrimingContext();
    private static final CallHistory callHistory = new CallHistory();
    private static final List<AppRequest> failedRequests = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModules(new JavaTimeModule(), new Jdk8Module())
            .enable(INDENT_OUTPUT)
            .setSerializationInclusion(NON_NULL)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    private static final Deserializer deserializer = new Deserializer(objectMapper);
    private static final AppRequestHandler appRequestHandler = new AppRequestHandler(primingContext, callHistory, failedRequests, new AppRequestFactory(deserializer));
    private static final PrimedMappingUploader primedMappingUploader = new PrimedMappingUploader(primingContext);
    private static final ZombieRequestHandler zombieRequestHandler = new ZombieRequestHandler(JzonbieOptions.options(), primingContext, callHistory, failedRequests, deserializer, new CurrentPrimingFileResponseFactory(objectMapper), primedMappingUploader);
    private static final Handlebars handlebars = new Handlebars();

    private AppRequest appRequest;
    private AppResponse appResponse;
    private ZombiePriming zombiePriming;

    @ClassRule
    public static PippoRule pippoRule = new PippoRule(new PippoApplication(JzonbieOptions.options(), appRequestHandler, zombieRequestHandler, objectMapper, singletonList(JzonbieRoute.get("/ready", c -> c.getRouteContext().getResponse().ok())), handlebars));


    @Before
    public void setUp() throws Exception {
        primingContext.clear();
        callHistory.clear();

        appRequest = AppRequestBuilderUtil.getFixturedAppRequest();
        appResponse = AppResponseBuilderUtil.getFixturedAppResponse();

        appResponse.setDelay(Duration.ZERO);

        zombiePriming = new ZombiePriming(appRequest, appResponse);
    }

    @Test
    public void testPriming() throws Exception {
        final Response pippoResponse = given()
                .header("zombie", "priming")
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(zombiePriming))
                .post("/");
        pippoResponse.then().statusCode(201);
        pippoResponse.then().contentType(ContentType.JSON);
        pippoResponse.then().body("request.path", equalTo(appRequest.getPath()));
        pippoResponse.then().body("response.statusCode", CoreMatchers.equalTo(appResponse.getStatusCode()));

        assertThat(primingContext.getCurrentPriming()).hasSize(1);
    }

    @Test
    public void testPrimingDefault() throws Exception {
        final Response pippoResponse = given()
                .header("zombie", "priming-default")
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(zombiePriming))
                .post("/");
        pippoResponse.then().statusCode(201);
        pippoResponse.then().contentType(ContentType.JSON);
        pippoResponse.then().body("request.path", equalTo(appRequest.getPath()));
        pippoResponse.then().body("response.statusCode", CoreMatchers.equalTo(appResponse.getStatusCode()));

        assertThat(primingContext.getCurrentPriming()).hasSize(1);

        final PrimedMapping mapping = primingContext.getCurrentPriming().get(0);

        assertThat(mapping.getAppResponses().getDefault().isPresent()).isTrue();
    }

    @Test
    public void testPrimingFile() throws Exception {
        final Response pippoResponse = given()
                .header("zombie", "priming-file")
                .contentType("multipart/form-data")
                .multiPart("priming", IoUtils.toString(getClass().getResourceAsStream("/example-priming.json")))
                .post("/");
        pippoResponse.then().statusCode(201);
        pippoResponse.then().contentType(ContentType.JSON);
        pippoResponse.then().body("[0].request.path", equalTo("/path"));
        pippoResponse.then().body("[0].responses.default.statusCode", CoreMatchers.equalTo(200));
        pippoResponse.then().body("[0].responses.default.body.key", equalTo("val"));
        pippoResponse.then().body("[0].responses.primed[0].statusCode", CoreMatchers.equalTo(201));
        pippoResponse.then().body("[0].responses.primed[0].body.key", equalTo("val"));

        assertThat(primingContext.getCurrentPriming()).hasSize(1);

        final PrimedMapping mapping = primingContext.getCurrentPriming().get(0);

        assertThat(mapping.getAppResponses().getDefault()).contains(staticDefault(
                ok()
                        .contentType("application/json")
                        .withBody(singletonMap("key", "val"))
                        .build()
        ));

        assertThat(mapping.getAppResponses().getEntries()).contains(
                created()
                        .contentType("application/json")
                        .withBody(singletonMap("key", "val"))
                        .build()
        );
    }

    @Test
    public void testPrimingWithDelay() throws Exception {
        zombiePriming.getAppResponse().setDelay(Duration.ofSeconds(10));

        final Response pippoResponse = given()
                .header("zombie", "priming")
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(zombiePriming))
                .post("/");
        pippoResponse.then().statusCode(201);
        pippoResponse.then().contentType(ContentType.JSON);
        pippoResponse.then().body("request.path", equalTo(appRequest.getPath()));
        pippoResponse.then().body("response.statusCode", CoreMatchers.equalTo(appResponse.getStatusCode()));
        pippoResponse.then().body("response.delay", equalTo(10.0f));

        assertThat(primingContext.getCurrentPriming()).hasSize(1);
    }

    @Test
    public void testCurrent() throws Exception {
        primingContext.add(zombiePriming);

        final Response pippoResponse = given()
                .header("zombie", "current")
                .contentType(ContentType.JSON)
                .post("/");
        pippoResponse.then().statusCode(200);
        pippoResponse.then().contentType(ContentType.JSON);
        pippoResponse.then().body("[0].request.path", equalTo(appRequest.getPath()));
        pippoResponse.then().body("[0].responses.default", nullValue());
        pippoResponse.then().body("[0].responses.primed[0].statusCode", CoreMatchers.equalTo(appResponse.getStatusCode()));
    }

    @Test
    public void testCurrentAsFile() throws Exception {
        primingContext.add(zombiePriming);

        final Response pippoResponse = given()
                .header("zombie", "current-file")
                .post("/");
        pippoResponse.then().statusCode(200);
        pippoResponse.then().contentType(ContentType.JSON);
        pippoResponse.then().header("content-disposition", startsWith("attachment; filename=\"jzonbie-current-priming"));
    }

    @Test
    public void testHistory() throws Exception {
        callHistory.add(zombiePriming);

        final Response pippoResponse = given()
                .header("zombie", "history")
                .contentType(ContentType.JSON)
                .post("/");
        pippoResponse.then().statusCode(200);
        pippoResponse.then().contentType(ContentType.JSON);
        pippoResponse.then().body("[0].request.path", equalTo(appRequest.getPath()));
        pippoResponse.then().body("[0].response.statusCode", CoreMatchers.equalTo(appResponse.getStatusCode()));
    }

    @Test
    public void testFailedRequests() throws Exception {
        failedRequests.add(appRequest);

        final Response pippoResponse = given()
                .header("zombie", "failed")
                .contentType(ContentType.JSON)
                .post("/");
        pippoResponse.then().statusCode(200);
        pippoResponse.then().contentType(ContentType.JSON);
        pippoResponse.then().body("[0].path", equalTo(appRequest.getPath()));
        pippoResponse.then().body("[0].method", equalTo(appRequest.getMethod()));
    }

    @Test
    public void testReset() throws Exception {
        primingContext.add(zombiePriming);
        callHistory.add(zombiePriming);
        failedRequests.add(appRequest);

        final Response pippoResponse = given()
                .header("zombie", "reset")
                .contentType(ContentType.JSON)
                .post("/");
        pippoResponse.then().statusCode(200);
        pippoResponse.then().contentType(ContentType.JSON);
        pippoResponse.then().body("message", equalTo("Zombie Reset"));

        assertThat(primingContext.getCurrentPriming()).isEmpty();
        assertThat(callHistory.getEntries()).isEmpty();
        assertThat(failedRequests).isEmpty();
    }

    @Test
    public void testAppRequest() throws Exception {
        final AppRequest request = AppRequest.get("/path").build();
        final AppResponse response = forbidden().build();

        primingContext.add(request, response);

        final Response pippoResponse = given()
                .contentType(ContentType.JSON)
                .get("/path");
        pippoResponse.then().statusCode(403);
        pippoResponse.then().body(equalTo(""));
    }

    @Test
    public void testAppRequestWithResponseDelay() throws Exception {
        final AppRequest request = AppRequest.get("/path").build();
        final AppResponse response = forbidden().withDelay(Duration.of(5, SECONDS)).build();

        primingContext.add(request, response);

        final Stopwatch stopwatch = Stopwatch.createStarted();

        final Response pippoResponse = given()
                .get("/path");

        stopwatch.stop();

        pippoResponse.then().statusCode(403);

        assertThat(stopwatch.elapsed(MILLISECONDS)).isGreaterThanOrEqualTo(5000);
    }

    @Test
    public void testAppRequestWithMapBodyPriming() throws Exception {
        final Map<String, String> requestBody = singletonMap("key", "val");
        final String errorMessage = "Something bad happened!";
        final AppRequest request = AppRequest.get("/path").withBody(requestBody).build();
        final AppResponse response = forbidden().contentType("application/json").withBody(singletonMap("error", errorMessage)).build();

        primingContext.add(request, response);

        final Response pippoResponse = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(requestBody))
                .get("/path");
        pippoResponse.then().statusCode(403);

        final Map<String, Object> responseBody = deserializer.deserialize(pippoResponse.getBody().asString());

        assertThat(responseBody).containsOnly(entry("error", errorMessage));
    }

    @Test
    public void testAppRequestWithStringBodyPriming() throws Exception {
        final String requestBody = "<jzonbie>message</jzonbie>";
        final String responseBody = "<error>Something bad happened!</error>";
        final AppRequest request = AppRequest.get("/path").withBody(requestBody).build();
        final AppResponse response = forbidden().contentType("application/xml").withBody(responseBody).build();

        primingContext.add(request, response);

        final Response pippoResponse = given()
                .body(requestBody)
                .get("/path");
        pippoResponse.then().statusCode(403);
        pippoResponse.then().body(equalTo(responseBody));
    }

    @Test
    public void testAppRequestWithJsonStringBodyPriming() throws Exception {
        final AppRequest request = AppRequest.get("/path").withBody(stringBody("request")).build();
        final AppResponse response = forbidden().withBody(stringBody("response")).build();

        primingContext.add(request, response);

        final Response pippoResponse = given()
                .body("\"request\"")
                .get("/path");
        pippoResponse.then().statusCode(403);
        pippoResponse.then().body(equalTo("\"response\""));
    }

    @Test
    public void testAppRequestWithListBodyPriming() throws Exception {
        final List<String> responseBody = singletonList("response1");
        final AppRequest request = AppRequest.get("/path").withBody(arrayBody(singletonList("request1"))).build();
        final AppResponse response = forbidden().contentType("application/json").withBody(arrayBody(responseBody)).build();

        primingContext.add(request, response);

        final Response pippoResponse = given()
                .body("[\"request1\"]")
                .get("/path");
        pippoResponse.then().statusCode(403);
        pippoResponse.then().body("[0]", equalTo("response1"));
    }

    @Test
    public void testAppRequestWithNumberBodyPriming() throws Exception {
        final AppRequest request = AppRequest.get("/path").withBody(1).build();
        final AppResponse response = forbidden().contentType("text/plain").withBody(2).build();

        primingContext.add(request, response);

        final Response pippoResponse = given()
                .body("1")
                .get("/path");
        pippoResponse.then().statusCode(403);
        pippoResponse.then().body(equalTo("2"));
    }

    @Test
    public void testCount() throws Exception {
        final AppRequest appRequest = AppRequest.get("/")
                .withBody(singletonMap("key", "val"))
                .build();
        final AppResponse appResponse = ok().build();

        callHistory.add(new ZombiePriming(appRequest, appResponse));

        final Response pippoResponse = given()
                .header("zombie", "count")
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(appRequest))
                .post("/");

        pippoResponse.then().assertThat()
                .statusCode(200)
                .body("count", equalTo(1));
    }

    @Test
    public void additionalRoutesCanBeAdded() throws Exception {
        final Response pippoResponse = given()
                .get("/ready");

        pippoResponse.then().assertThat()
                .statusCode(200);
    }

    @Test
    public void testTemplatedPriming() throws Exception {
        final AppResponse appResponse = AppResponse.ok().withBody(literalBody("{{ request.path }}")).build();
        zombiePriming.setAppResponse(appResponse);

        final Response pippoResponse = given()
                .header("zombie", "priming-template")
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(zombiePriming))
                .post("/");
        pippoResponse.then().statusCode(201);
        pippoResponse.then().contentType(ContentType.JSON);
        pippoResponse.then().body("request.path", equalTo(appRequest.getPath()));
        pippoResponse.then().body("response.statusCode", CoreMatchers.equalTo(appResponse.getStatusCode()));

        final List<PrimedMapping> currentPriming = primingContext.getCurrentPriming();
        assertThat(currentPriming).hasSize(1);
        assertThat(currentPriming.get(0).getAppResponses().getEntries().get(0)).isInstanceOf(TemplatedAppResponse.class);
    }

    @Test
    public void testTemplatedDefaultPriming() throws Exception {
        final AppResponse appResponse = AppResponse.ok().withBody(literalBody("{{ request.path }}")).build();
        zombiePriming.setAppResponse(appResponse);

        final Response pippoResponse = given()
                .header("zombie", "priming-default-template")
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(zombiePriming))
                .post("/");
        pippoResponse.then().statusCode(201);
        pippoResponse.then().contentType(ContentType.JSON);
        pippoResponse.then().body("request.path", equalTo(appRequest.getPath()));
        pippoResponse.then().body("response.statusCode", CoreMatchers.equalTo(appResponse.getStatusCode()));

        final List<PrimedMapping> currentPriming = primingContext.getCurrentPriming();
        assertThat(currentPriming).hasSize(1);
        assertThat(currentPriming.get(0).getAppResponses().getDefault().get().getResponse()).isInstanceOf(TemplatedAppResponse.class);
    }

    @Test
    public void testAppRequestWithTemplatedPriming() throws Exception {
        final AppRequest request = AppRequest.get("/path").build();
        final TemplatedAppResponse response = templated(
                ok().withHeader("method", "{{ request.method }}")
                        .withBody(literalBody("{\"path\": \"{{ request.path }}\"}"))
                        .build()
        );

        primingContext.add(request, response);

        final Response pippoResponse = given()
                .get("/path");

        pippoResponse.then()
                .header("method", "GET")
                .body(equalTo("{\"path\": \"/path\"}"));
    }
}