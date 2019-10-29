package com.jonnymatts.jzonbie.pippo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.jonnymatts.jzonbie.body.ObjectBodyContent;
import com.jonnymatts.jzonbie.history.CallHistory;
import com.jonnymatts.jzonbie.history.Exchange;
import com.jonnymatts.jzonbie.history.FixedCapacityCache;
import com.jonnymatts.jzonbie.jackson.Deserializer;
import com.jonnymatts.jzonbie.jackson.JzonbieObjectMapper;
import com.jonnymatts.jzonbie.priming.AppRequestFactory;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.priming.PrimingContext;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.requests.AppRequestHandler;
import com.jonnymatts.jzonbie.requests.PrimedMappingUploader;
import com.jonnymatts.jzonbie.requests.ZombieRequestHandler;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.CurrentPrimingFileResponseFactory;
import com.jonnymatts.jzonbie.ssl.HttpsSupport;
import com.jonnymatts.jzonbie.templating.JzonbieHandlebars;
import com.jonnymatts.jzonbie.templating.ResponseTransformer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.pippo.core.Pippo;
import ro.pippo.core.util.IoUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.jonnymatts.jzonbie.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.body.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.responses.AppResponse.*;
import static com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse.staticDefault;
import static io.restassured.RestAssured.given;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.startsWith;

class PippoApplicationTest {

    private static PrimingContext primingContext = new PrimingContext();
    private static final CallHistory callHistory = new CallHistory(3);
    private static final FixedCapacityCache<AppRequest> failedRequests = new FixedCapacityCache<>(3);
    private static final ObjectMapper objectMapper = new JzonbieObjectMapper();
    private static final Deserializer deserializer = new Deserializer(objectMapper);
    private static final AppRequestHandler appRequestHandler = new AppRequestHandler(primingContext, callHistory, failedRequests, new AppRequestFactory(deserializer));
    private static final PrimedMappingUploader primedMappingUploader = new PrimedMappingUploader(primingContext);
    private static final ZombieRequestHandler zombieRequestHandler = new ZombieRequestHandler("zombie", primingContext, callHistory, failedRequests, deserializer, new CurrentPrimingFileResponseFactory(objectMapper), primedMappingUploader, new HttpsSupport());
    private static final ResponseTransformer responseTransformer = new ResponseTransformer(objectMapper, new JzonbieHandlebars());
    private static final PippoResponder pippoResponder = new PippoResponder(objectMapper);

    private AppRequest appRequest;
    private AppResponse appResponse;
    private ZombiePriming zombiePriming;
    private Exchange exchange;

    @BeforeAll
    static void beforeAll() {
        final PippoApplication application = new PippoApplication("zombie", singletonList(JzonbieRoute.get("/ready", c -> c.getRouteContext().getResponse().ok())), appRequestHandler, zombieRequestHandler, pippoResponder, responseTransformer);
        final Pippo pippo = new Pippo(application);
        pippo.start();
        RestAssured.port = pippo.getServer().getPort();
    }

    @BeforeEach
    void setUp() throws Exception {
        primingContext.reset();
        callHistory.clear();
        failedRequests.clear();

        appRequest = AppRequest.get("");
        appResponse = ok();

        appResponse.setDelay(Duration.ZERO);

        zombiePriming = new ZombiePriming(appRequest, appResponse);
        exchange = new Exchange(appRequest, appResponse);
    }

    @Test
    void testPriming() throws Exception {
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
    void testPrimingDefault() throws Exception {
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

        assertThat(mapping.getResponses().getDefault().isPresent()).isTrue();
    }

    @Test
    void testPrimingFile() throws Exception {
        final Response pippoResponse = given()
                .header("zombie", "priming-file")
                .contentType("multipart/form-data")
                .multiPart("priming", IoUtils.toString(getClass().getResourceAsStream("/example-priming.json")))
                .post("/");
        pippoResponse.then().statusCode(201);
        pippoResponse.then().contentType(ContentType.JSON);
        pippoResponse.then().body("[0].request.path", equalTo("/path"));
        pippoResponse.then().body("[0].responses.default.static.statusCode", CoreMatchers.equalTo(200));
        pippoResponse.then().body("[0].responses.default.static.body.object.key", equalTo("val"));
        pippoResponse.then().body("[0].responses.primed[0].statusCode", CoreMatchers.equalTo(201));
        pippoResponse.then().body("[0].responses.primed[0].body.object.key", equalTo("val"));

        assertThat(primingContext.getCurrentPriming()).hasSize(1);

        final PrimedMapping mapping = primingContext.getCurrentPriming().get(0);

        assertThat(mapping.getResponses().getDefault()).contains(staticDefault(
                ok()
                        .contentType("application/json")
                        .withBody(objectBody(singletonMap("key", "val")))
        ));

        assertThat(mapping.getResponses().getPrimed()).contains(
                created()
                        .contentType("application/json")
                        .withBody(objectBody(singletonMap("key", "val")))
        );
    }

    @Test
    void testPrimingWithDelay() throws Exception {
        zombiePriming.getResponse().setDelay(Duration.ofSeconds(10));

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
    void testCurrent() throws Exception {
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
    void testCurrentAsFile() throws Exception {
        primingContext.add(zombiePriming);

        final Response pippoResponse = given()
                .header("zombie", "current-file")
                .post("/");
        pippoResponse.then().statusCode(200);
        pippoResponse.then().contentType(ContentType.JSON);
        pippoResponse.then().header("content-disposition", startsWith("attachment; filename=\"jzonbie-current-priming"));
    }

    @Test
    void testHistory() throws Exception {
        callHistory.add(appRequest, exchange);

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
    void testFailedRequests() throws Exception {
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
    void testReset() throws Exception {
        primingContext.add(zombiePriming);
        callHistory.add(appRequest, exchange);
        failedRequests.add(appRequest);

        final Response pippoResponse = given()
                .header("zombie", "reset")
                .contentType(ContentType.JSON)
                .post("/");
        pippoResponse.then().statusCode(200);
        pippoResponse.then().contentType(ContentType.JSON);
        pippoResponse.then().body("message", equalTo("Zombie Reset"));

        assertThat(primingContext.getCurrentPriming()).isEmpty();
        assertThat(callHistory.getValues()).isEmpty();
        assertThat(failedRequests.getValues()).isEmpty();
    }

    @Test
    void testAppRequest() throws Exception {
        final AppRequest request = AppRequest.get("/path");
        final AppResponse response = forbidden();

        primingContext.add(request, response);

        final Response pippoResponse = given()
                .contentType(ContentType.JSON)
                .get("/path");
        pippoResponse.then().statusCode(403);
        pippoResponse.then().body(equalTo(""));
    }

    @Test
    void testAppRequestWithResponseDelay() throws Exception {
        final AppRequest request = AppRequest.get("/path");
        final AppResponse response = forbidden().withDelay(Duration.of(5, SECONDS));

        primingContext.add(request, response);

        final Stopwatch stopwatch = Stopwatch.createStarted();

        final Response pippoResponse = given()
                .get("/path");

        stopwatch.stop();

        pippoResponse.then().statusCode(403);

        assertThat(stopwatch.elapsed(MILLISECONDS)).isGreaterThanOrEqualTo(5000);
    }

    @Test
    void testAppRequestWithMapBodyPriming() throws Exception {
        final Map<String, String> requestBody = singletonMap("key", "val");
        final String errorMessage = "Something bad happened!";
        final AppRequest request = AppRequest.get("/path").withBody(objectBody(requestBody));
        final AppResponse response = forbidden().contentType("application/json").withBody(objectBody(singletonMap("error", errorMessage)));

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
    void testAppRequestWithLiteralBodyPriming() throws Exception {
        final String requestBody = "<jzonbie>message</jzonbie>";
        final String responseBody = "<error>Something bad happened!</error>";
        final AppRequest request = AppRequest.get("/path").withBody(literalBody(requestBody));
        final AppResponse response = forbidden().contentType("application/xml").withBody(literalBody(responseBody));

        primingContext.add(request, response);

        final Response pippoResponse = given()
                .body(requestBody)
                .get("/path");
        pippoResponse.then().statusCode(403);
        pippoResponse.then().body(equalTo(responseBody));
    }

    @Test
    void testAppRequestWithJsonStringBodyPriming() throws Exception {
        final AppRequest request = AppRequest.get("/path").withBody(stringBody("request"));
        final AppResponse response = forbidden().withBody(stringBody("response"));

        primingContext.add(request, response);

        final Response pippoResponse = given()
                .body("\"request\"")
                .get("/path");
        pippoResponse.then().statusCode(403);
        pippoResponse.then().body(equalTo("\"response\""));
    }

    @Test
    void testAppRequestWithListBodyPriming() throws Exception {
        final List<String> responseBody = singletonList("response1");
        final AppRequest request = AppRequest.get("/path").withBody(arrayBody(singletonList("request1")));
        final AppResponse response = forbidden().contentType("application/json").withBody(arrayBody(responseBody));

        primingContext.add(request, response);

        final Response pippoResponse = given()
                .body("[\"request1\"]")
                .get("/path");
        pippoResponse.then().statusCode(403);
        pippoResponse.then().body("[0]", equalTo("response1"));
    }

    @Test
    void testAppRequestWithNumberBodyPriming() throws Exception {
        final AppRequest request = AppRequest.get("/path").withBody(literalBody(1));
        final AppResponse response = forbidden().contentType("text/plain").withBody(literalBody(2));

        primingContext.add(request, response);

        final Response pippoResponse = given()
                .body("1")
                .get("/path");
        pippoResponse.then().statusCode(403);
        pippoResponse.then().body(equalTo("2"));
    }

    @Test
    void testCount() throws Exception {
        final AppRequest appRequest = AppRequest.get("/")
                .withBody(objectBody(singletonMap("key", "val")));
        final AppResponse appResponse = ok();

        callHistory.add(appRequest, new Exchange(appRequest, appResponse));

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
    void testReturn404NotFoundWhenNoMatchingPrimingLocated() throws Exception {
        final Response pippoResponse = given()
                .get("/");

        pippoResponse.then().assertThat()
                .body(containsString("\"message\" : \"Priming not found for request\""))
                .statusCode(404);
    }

    @Test
    void testReturn500InternalErrorWhenUnexpectedErrorOccurs() throws Exception {
        final AppRequest appRequest = AppRequest.get("/");
        final AppResponse appResponse = ok().withBody(ObjectBodyContent.objectBody(singletonMap("{messedUpJson}", "{{{{{}} {messedUpJson = 12}"))).templated();

        primingContext.add(appRequest, appResponse);

        final Response pippoResponse = given()
                .get("/");

        pippoResponse.then().assertThat()
                .body(containsString("Could not transform: {\\n  \\\"{messedUpJson}\\\" : \\\"{{{{{}} {messedUpJson = 12}\\\"\\n}\""))
                .statusCode(500);
    }

    @Test
    void additionalRoutesCanBeAdded() throws Exception {
        final Response pippoResponse = given()
                .get("/ready");

        pippoResponse.then().assertThat()
                .statusCode(200);
    }

    @Test
    void testAppRequestWithTemplatedPriming() throws Exception {
        final AppRequest request = AppRequest.get("/path");
        final AppResponse response =
                ok().withHeader("method", "{{ request.method }}")
                        .withBody(literalBody("{\"path\": \"{{ request.path }}\"}"))
                        .templated();

        primingContext.add(request, response);

        final Response pippoResponse = given()
                .get("/path");

        pippoResponse.then()
                .header("method", "GET")
                .body(equalTo("{\"path\": \"/path\"}"));
    }

    @Test
    void testAppRequestWithRequestCounterTemplatePriming() throws Exception {
        final AppRequest request = AppRequest.get("/path");
        final AppResponse response =
                ok().withHeader("requestCounter", "{{ ENDPOINT_REQUEST_COUNT }}")
                        .withBody(literalBody("{\"requestCounter\": \"{{ ENDPOINT_REQUEST_COUNT }}\"}"))
                        .templated();

        primingContext.add(request, response);
        primingContext.add(request, response);

        final Response pippoResponse1 = given()
                .get("/path");

        pippoResponse1.then()
                .header("requestCounter", "1")
                .body(equalTo("{\"requestCounter\": \"1\"}"));

        final Response pippoResponse2 = given()
                .get("/path");

        pippoResponse2.then()
                .header("requestCounter", "2")
                .body(equalTo("{\"requestCounter\": \"2\"}"));
    }


    @Test
    void testUp() {
        final Response pippoResponse = given()
                .header("zombie", "up")
                .get("/");
        pippoResponse.then().statusCode(200);
        pippoResponse.then().body("message", equalTo("Up!"));
    }
}