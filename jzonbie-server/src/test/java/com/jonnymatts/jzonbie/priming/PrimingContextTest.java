package com.jonnymatts.jzonbie.priming;

import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.responses.DefaultAppResponse;
import com.jonnymatts.jzonbie.responses.DefaultAppResponse.StaticDefaultAppResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.jonnymatts.jzonbie.defaults.StandardDefaultPriming.defaultPriming;
import static com.jonnymatts.jzonbie.priming.AppRequest.get;
import static com.jonnymatts.jzonbie.priming.AppResponse.internalServerError;
import static com.jonnymatts.jzonbie.priming.AppResponse.ok;
import static com.jonnymatts.jzonbie.priming.Cloner.cloneRequest;
import static com.jonnymatts.jzonbie.responses.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class PrimingContextTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    private ZombiePriming zombiePriming;

    private PrimingContext primingContext;

    @Before
    public void setUp() throws Exception {
        primingContext = new PrimingContext();
        zombiePriming = new ZombiePriming(
                get("/path")
                        .withHeader("header", "value")
                        .withQueryParam("param", "value")
                        .withBody(singletonMap("bodyKey", "bodyVal"))
                        .build(),
                ok().build()
        );
    }

    @Test
    public void getCurrentPrimingReturnsListOfPrimedMappings() throws Exception {
        primingContext.add(zombiePriming);

        final List<PrimedMapping> got = primingContext.getCurrentPriming();

        assertThat(got).hasSize(1);
        assertThat(got.get(0).getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(got.get(0).getAppResponses().getEntries()).containsExactly(zombiePriming.getAppResponse());
    }

    @Test
    public void addReturnsPrimingContextWithNewPrimingAdded() throws Exception {
        assertThat(primingContext.getCurrentPriming()).isEmpty();

        final PrimingContext got = primingContext.add(zombiePriming);

        final List<PrimedMapping> currentPriming = got.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());

        final List<AppResponse> entries = primedMapping.getAppResponses().getEntries();

        assertThat(entries).hasSize(1);
        assertThat(entries).containsExactly(zombiePriming.getAppResponse());
    }

    @Test
    public void addReturnsPrimingContextWithNewPrimingAddedForAppRequestAndAppResponseInputs() throws Exception {
        assertThat(primingContext.getCurrentPriming()).isEmpty();

        final PrimingContext got = primingContext.add(zombiePriming.getAppRequest(), zombiePriming.getAppResponse());

        final List<PrimedMapping> currentPriming = got.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());

        final List<AppResponse> entries = primedMapping.getAppResponses().getEntries();

        assertThat(entries).hasSize(1);
        assertThat(entries).containsExactly(zombiePriming.getAppResponse());
    }

    @Test
    public void addReturnsPrimingContextWithPrimingAddedAddedToExistingPrimedMappingIfMappingAlreadyExistsWithAppResponse() throws Exception {
        primingContext.add(zombiePriming);

        final PrimingContext got = primingContext.add(zombiePriming);

        final List<PrimedMapping> currentPriming = got.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());

        final List<AppResponse> entries = primedMapping.getAppResponses().getEntries();

        assertThat(entries).hasSize(2);
        assertThat(entries).containsExactly(zombiePriming.getAppResponse(), zombiePriming.getAppResponse());
    }

    @Test
    public void addDefaultReturnsPrimingContextWithDefaultPrimingAddedForAlreadyExistingRequest() throws Exception {
        primingContext.add(zombiePriming);

        final StaticDefaultAppResponse defaultResponse = staticDefault(zombiePriming.getAppResponse());

        final PrimingContext got = primingContext.addDefault(zombiePriming.getAppRequest(), defaultResponse);

        final List<PrimedMapping> currentPriming = got.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getAppResponses().getDefault().map(DefaultAppResponse::getResponse)).contains(zombiePriming.getAppResponse());
    }

    @Test
    public void addDefaultReturnsPrimingContextWithDefaultPrimingAddedForNewPriming() throws Exception {
        final StaticDefaultAppResponse defaultResponse = staticDefault(zombiePriming.getAppResponse());

        final PrimingContext got = primingContext.addDefault(zombiePriming.getAppRequest(), defaultResponse);

        final List<PrimedMapping> currentPriming = got.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getAppResponses().getDefault().map(DefaultAppResponse::getResponse)).contains(zombiePriming.getAppResponse());
    }

    @Test
    public void getResponseReturnsOptionalOfAppResponseIfPrimingExistsForAppRequest() throws Exception {
        primingContext.add(zombiePriming);

        final Optional<AppResponse> got = primingContext.getResponse(zombiePriming.getAppRequest());

        assertThat(got.isPresent()).isTrue();
        assertThat(got).contains(zombiePriming.getAppResponse());
    }

    @Test
    public void getResponseRemovesFirstAppResponseFromPrimingIfMultipleResponsesExistForPrimingOfAppRequest() throws Exception {
        primingContext.add(zombiePriming);
        primingContext.add(zombiePriming);

        primingContext.getResponse(zombiePriming.getAppRequest());

        final List<PrimedMapping> currentPriming = primingContext.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());

        final List<AppResponse> entries = primedMapping.getAppResponses().getEntries();

        assertThat(entries).hasSize(1);
        assertThat(entries).containsExactly(zombiePriming.getAppResponse());
    }

    @Test
    public void getResponseRemovesPrimingFromContextIfSingleResponseExistsForPrimingOfAppRequest() throws Exception {
        primingContext.add(zombiePriming);

        primingContext.getResponse(zombiePriming.getAppRequest());

        final List<PrimedMapping> currentPriming = primingContext.getCurrentPriming();

        assertThat(currentPriming).hasSize(0);
    }

    @Test
    public void getResponseRemovesPrimingFromContextIfSingleResponseExistsForPrimingOfAppRequestIgnoringExtraHeadersOnIncomingRequest() throws Exception {
        primingContext.add(zombiePriming);

        final AppRequest copy = cloneRequest(zombiePriming.getAppRequest());
        final HashMap<String, String> headersCopy = new HashMap<>(zombiePriming.getAppRequest().getHeaders());
        headersCopy.put("extra", "header");
        copy.setHeaders(headersCopy);

        primingContext.getResponse(copy);

        final List<PrimedMapping> currentPriming = primingContext.getCurrentPriming();

        assertThat(currentPriming).hasSize(0);
    }

    @Test
    public void getResponseReturnsEmptyOptionalIfPrimingDoesNotExistForAppRequest() throws Exception {
        final Optional<AppResponse> got = primingContext.getResponse(zombiePriming.getAppRequest());

        assertThat(got.isPresent()).isFalse();
    }

    @Test
    public void resetResetsTheCurrentPriming() throws Exception {
        primingContext.add(zombiePriming);

        assertThat(primingContext.getCurrentPriming()).hasSize(1);

        primingContext.reset();

        assertThat(primingContext.getCurrentPriming()).hasSize(0);
    }

    @Test
    public void getResponseReturnsRightResponseWhenRequestIsIdenticalToAnotherRequestExceptForHeaders() throws Exception {
        primingContext.add(zombiePriming);

        final AppRequest appRequest = zombiePriming.getAppRequest();
        final AppRequest copy = cloneRequest(appRequest);
        copy.setHeaders(singletonMap("key", "val"));
        final AppResponse response = internalServerError().build();
        primingContext.add(new ZombiePriming(copy, response));

        final Optional<AppResponse> got = primingContext.getResponse(copy);

        assertThat(got).contains(response);
    }

    @Test
    public void defaultPrimingIsAdded() {
        primingContext = new PrimingContext(singletonList(defaultPriming(zombiePriming.getAppRequest(), zombiePriming.getAppResponse())));

        final List<PrimedMapping> currentPriming = primingContext.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());

        final List<AppResponse> entries = primedMapping.getAppResponses().getEntries();

        assertThat(entries).hasSize(1);
        assertThat(entries).containsExactly(zombiePriming.getAppResponse());
    }

    @Test
    public void defaultPrimingIsAddedAfterReset() {
        primingContext = new PrimingContext(singletonList(defaultPriming(zombiePriming.getAppRequest(), zombiePriming.getAppResponse())));
        primingContext.reset();

        final List<PrimedMapping> currentPriming = primingContext.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());

        final List<AppResponse> entries = primedMapping.getAppResponses().getEntries();

        assertThat(entries).hasSize(1);
        assertThat(entries).containsExactly(zombiePriming.getAppResponse());
    }
}