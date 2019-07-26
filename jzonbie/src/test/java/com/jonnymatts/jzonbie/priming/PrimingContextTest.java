package com.jonnymatts.jzonbie.priming;

import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;
import com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.jonnymatts.jzonbie.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.defaults.StandardPriming.priming;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.internalServerError;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse.staticDefault;
import static com.jonnymatts.jzonbie.util.Cloner.cloneRequest;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class PrimingContextTest {

    private ZombiePriming zombiePriming;

    private PrimingContext primingContext;

    @BeforeEach
    void setUp() throws Exception {
        primingContext = new PrimingContext();
        zombiePriming = new ZombiePriming(
                get("/path")
                        .withHeader("header", "value")
                        .withQueryParam("param", "value")
                        .withBody(objectBody(singletonMap("bodyKey", "bodyVal")))
                        .build(),
                ok().build()
        );
    }

    @Test
    void getCurrentPrimingReturnsListOfPrimedMappings() throws Exception {
        primingContext.add(zombiePriming);

        final List<PrimedMapping> got = primingContext.getCurrentPriming();

        assertThat(got).hasSize(1);
        assertThat(got.get(0).getRequest()).isEqualTo(zombiePriming.getRequest());
        assertThat(got.get(0).getResponses().getPrimed()).containsExactly(zombiePriming.getResponse());
    }

    @Test
    void addReturnsPrimingContextWithNewPrimingAdded() throws Exception {
        assertThat(primingContext.getCurrentPriming()).isEmpty();

        final PrimingContext got = primingContext.add(zombiePriming);

        final List<PrimedMapping> currentPriming = got.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());

        final List<AppResponse> entries = primedMapping.getResponses().getPrimed();

        assertThat(entries).hasSize(1);
        assertThat(entries).containsExactly(zombiePriming.getResponse());
    }

    @Test
    void addReturnsPrimingContextWithNewPrimingAddedForAppRequestAndAppResponseInputs() throws Exception {
        assertThat(primingContext.getCurrentPriming()).isEmpty();

        final PrimingContext got = primingContext.add(zombiePriming.getRequest(), zombiePriming.getResponse());

        final List<PrimedMapping> currentPriming = got.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());

        final List<AppResponse> entries = primedMapping.getResponses().getPrimed();

        assertThat(entries).hasSize(1);
        assertThat(entries).containsExactly(zombiePriming.getResponse());
    }

    @Test
    void addReturnsPrimingContextWithPrimingAddedAddedToExistingPrimedMappingIfMappingAlreadyExistsWithAppResponse() throws Exception {
        primingContext.add(zombiePriming);

        final PrimingContext got = primingContext.add(zombiePriming);

        final List<PrimedMapping> currentPriming = got.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());

        final List<AppResponse> entries = primedMapping.getResponses().getPrimed();

        assertThat(entries).hasSize(2);
        assertThat(entries).containsExactly(zombiePriming.getResponse(), zombiePriming.getResponse());
    }

    @Test
    void addDefaultReturnsPrimingContextWithDefaultPrimingAddedForAlreadyExistingRequest() throws Exception {
        primingContext.add(zombiePriming);

        final StaticDefaultAppResponse defaultResponse = staticDefault(zombiePriming.getResponse());

        final PrimingContext got = primingContext.addDefault(zombiePriming.getRequest(), defaultResponse);

        final List<PrimedMapping> currentPriming = got.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getResponses().getDefault().map(DefaultAppResponse::getResponse)).contains(zombiePriming.getResponse());
    }

    @Test
    void addDefaultReturnsPrimingContextWithDefaultPrimingAddedForNewPriming() throws Exception {
        final StaticDefaultAppResponse defaultResponse = staticDefault(zombiePriming.getResponse());

        final PrimingContext got = primingContext.addDefault(zombiePriming.getRequest(), defaultResponse);

        final List<PrimedMapping> currentPriming = got.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getResponses().getDefault().map(DefaultAppResponse::getResponse)).contains(zombiePriming.getResponse());
    }

    @Test
    void getResponseReturnsOptionalOfAppResponseIfPrimingExistsForAppRequest() throws Exception {
        primingContext.add(zombiePriming);

        final Optional<AppResponse> got = primingContext.getResponse(zombiePriming.getRequest());

        assertThat(got.isPresent()).isTrue();
        assertThat(got).contains(zombiePriming.getResponse());
    }

    @Test
    void getResponseRemovesFirstAppResponseFromPrimingIfMultipleResponsesExistForPrimingOfAppRequest() throws Exception {
        primingContext.add(zombiePriming);
        primingContext.add(zombiePriming);

        primingContext.getResponse(zombiePriming.getRequest());

        final List<PrimedMapping> currentPriming = primingContext.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());

        final List<AppResponse> entries = primedMapping.getResponses().getPrimed();

        assertThat(entries).hasSize(1);
        assertThat(entries).containsExactly(zombiePriming.getResponse());
    }

    @Test
    void getResponseRemovesPrimingFromContextIfSingleResponseExistsForPrimingOfAppRequest() throws Exception {
        primingContext.add(zombiePriming);

        primingContext.getResponse(zombiePriming.getRequest());

        final List<PrimedMapping> currentPriming = primingContext.getCurrentPriming();

        assertThat(currentPriming).hasSize(0);
    }

    @Test
    void getResponseRemovesPrimingFromContextIfSingleResponseExistsForPrimingOfAppRequestIgnoringExtraHeadersOnIncomingRequest() throws Exception {
        primingContext.add(zombiePriming);

        final AppRequest copy = cloneRequest(zombiePriming.getRequest());
        final HashMap<String, String> headersCopy = new HashMap<>(zombiePriming.getRequest().getHeaders());
        headersCopy.put("extra", "header");
        copy.setHeaders(headersCopy);

        primingContext.getResponse(copy);

        final List<PrimedMapping> currentPriming = primingContext.getCurrentPriming();

        assertThat(currentPriming).hasSize(0);
    }

    @Test
    void getResponseReturnsEmptyOptionalIfPrimingDoesNotExistForAppRequest() throws Exception {
        final Optional<AppResponse> got = primingContext.getResponse(zombiePriming.getRequest());

        assertThat(got.isPresent()).isFalse();
    }

    @Test
    void resetResetsTheCurrentPriming() throws Exception {
        primingContext.add(zombiePriming);

        assertThat(primingContext.getCurrentPriming()).hasSize(1);

        primingContext.reset();

        assertThat(primingContext.getCurrentPriming()).hasSize(0);
    }

    @Test
    void getResponseReturnsRightResponseWhenRequestIsIdenticalToAnotherRequestExceptForHeaders() throws Exception {
        primingContext.add(zombiePriming);

        final AppRequest appRequest = zombiePriming.getRequest();
        final AppRequest copy = cloneRequest(appRequest);
        copy.setHeaders(singletonMap("key", "val"));
        final AppResponse response = internalServerError().build();
        primingContext.add(new ZombiePriming(copy, response));

        final Optional<AppResponse> got = primingContext.getResponse(copy);

        assertThat(got).contains(response);
    }

    @Test
    void defaultPrimingIsAdded() {
        primingContext = new PrimingContext(singletonList(priming(zombiePriming.getRequest(), zombiePriming.getResponse())));

        final List<PrimedMapping> currentPriming = primingContext.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());

        final List<AppResponse> entries = primedMapping.getResponses().getPrimed();

        assertThat(entries).hasSize(1);
        assertThat(entries).containsExactly(zombiePriming.getResponse());
    }

    @Test
    void defaultPrimingIsAddedAfterReset() {
        primingContext = new PrimingContext(singletonList(priming(zombiePriming.getRequest(), zombiePriming.getResponse())));
        primingContext.reset();

        final List<PrimedMapping> currentPriming = primingContext.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getRequest()).isEqualTo(zombiePriming.getRequest());

        final List<AppResponse> entries = primedMapping.getResponses().getPrimed();

        assertThat(entries).hasSize(1);
        assertThat(entries).containsExactly(zombiePriming.getResponse());
    }
}