package com.jonnymatts.jzonbie.pippo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Stopwatch;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jonnymatts.jzonbie.JzonbieOptions;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.requests.AppRequestHandler;
import com.jonnymatts.jzonbie.requests.ZombieRequestHandler;
import com.jonnymatts.jzonbie.response.CurrentPrimingFileResponseFactory;
import com.jonnymatts.jzonbie.util.AppRequestBuilderUtil;
import com.jonnymatts.jzonbie.util.AppResponseBuilderUtil;
import com.jonnymatts.jzonbie.util.Deserializer;
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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

@RunWith(MockitoJUnitRunner.class)
public class PippoApplicationTest extends PippoTest {

    private static PrimingContext primingContext = new PrimingContext();
    private static final List<ZombiePriming> callHistory = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper().enable(INDENT_OUTPUT).findAndRegisterModules().setSerializationInclusion(NON_NULL).configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    private static final Deserializer deserializer = new Deserializer(objectMapper);
    private static final AppRequestHandler appRequestHandler = new AppRequestHandler(primingContext, callHistory, new AppRequestFactory(deserializer));
    private static final ZombieRequestHandler zombieRequestHandler = new ZombieRequestHandler(JzonbieOptions.options(), primingContext, callHistory, deserializer, new CurrentPrimingFileResponseFactory(objectMapper));

    private AppRequest appRequest;
    private AppResponse appResponse;
    private ZombiePriming zombiePriming;

    @ClassRule
    public static PippoRule pippoRule = new PippoRule(new PippoApplication(JzonbieOptions.options(), appRequestHandler, zombieRequestHandler, objectMapper));


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
        pippoResponse.then().body("response.statusCode", equalTo(appResponse.getStatusCode()));

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
        pippoResponse.then().body("response.statusCode", equalTo(appResponse.getStatusCode()));

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
        pippoResponse.then().body("[0].responses.default.statusCode", equalTo(200));
        pippoResponse.then().body("[0].responses.primed[0].statusCode", equalTo(201));
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
        pippoResponse.then().body("response.statusCode", equalTo(appResponse.getStatusCode()));
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
        pippoResponse.then().body("[0].responses.primed[0].statusCode", equalTo(appResponse.getStatusCode()));
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
        pippoResponse.then().body("[0].response.statusCode", equalTo(appResponse.getStatusCode()));
    }

    @Test
    public void testReset() throws Exception {
        primingContext.add(zombiePriming);
        callHistory.add(zombiePriming);

        final Response pippoResponse = given()
                .header("zombie", "reset")
                .contentType(ContentType.JSON)
                .post("/");
        pippoResponse.then().statusCode(200);
        pippoResponse.then().contentType(ContentType.JSON);

        assertThat(primingContext.getCurrentPriming()).isEmpty();
        assertThat(callHistory).isEmpty();
    }

    @Test
    public void testAppRequest() throws Exception {
        final AppRequest request = AppRequest.builder("GET", "/path").build();
        final AppResponse response = AppResponse.builder(403).build();

        primingContext.add(request, response);

        final Response pippoResponse = given()
                .contentType(ContentType.JSON)
                .get("/path");
        pippoResponse.then().statusCode(403);
    }

    @Test
    public void testAppRequestWithResponseDelay() throws Exception {
        final AppRequest request = AppRequest.builder("GET", "/path").build();
        final AppResponse response = AppResponse.builder(403).withDelay(Duration.of(5, SECONDS)).build();

        primingContext.add(request, response);

        final Stopwatch stopwatch = Stopwatch.createStarted();

        final Response pippoResponse = given()
                .get("/path");

        stopwatch.stop();

        pippoResponse.then().statusCode(403);

        assertThat(stopwatch.elapsed(MILLISECONDS)).isGreaterThanOrEqualTo(5000);
    }
}