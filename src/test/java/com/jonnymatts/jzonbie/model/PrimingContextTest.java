package com.jonnymatts.jzonbie.model;

import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class PrimingContextTest {

    @Rule
    public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Fixture
    private ZombiePriming zombiePriming;

    private List<PrimedMapping> primedMappings;
    private PrimingContext primingContext;

    @Before
    public void setUp() throws Exception {
        primedMappings = new ArrayList<>();
        primingContext = new PrimingContext(primedMappings);
    }

    @Test
    public void getCurrentPrimingReturnsListOfPrimedMappings() throws Exception {
        primedMappings.add(new PrimedMapping(zombiePriming.getAppRequest(), singletonList(zombiePriming.getAppResponse())));

        final List<PrimedMapping> got = primingContext.getCurrentPriming();

        assertThat(got).hasSize(1);
        assertThat(got.get(0).getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(got.get(0).getAppResponses()).containsExactly(zombiePriming.getAppResponse());
    }

    @Test
    public void addReturnsPrimingContextWithNewPrimingAdded() throws Exception {
        assertThat(primingContext.getCurrentPriming()).isEmpty();

        final PrimingContext got = primingContext.add(zombiePriming);

        final List<PrimedMapping> currentPriming = got.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getAppResponses()).hasSize(1);
        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses()).containsExactly(zombiePriming.getAppResponse());
    }

    @Test
    public void addReturnsPrimingContextWithNewPrimingAddedForAppRequestAndAppResponseInputs() throws Exception {
        assertThat(primingContext.getCurrentPriming()).isEmpty();

        final PrimingContext got = primingContext.add(zombiePriming.getAppRequest(), zombiePriming.getAppResponse());

        final List<PrimedMapping> currentPriming = got.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getAppResponses()).hasSize(1);
        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses()).containsExactly(zombiePriming.getAppResponse());
    }

    @Test
    public void addReturnsPrimingContextWithPrimingAddedAddedToExistingPrimedMappingIfMappingAlreadyExistsWithAppResponse() throws Exception {
        primedMappings.add(new PrimedMapping(zombiePriming.getAppRequest(), new ArrayList<AppResponse>(){{
            add(zombiePriming.getAppResponse());
        }}));

        final PrimingContext got = primingContext.add(zombiePriming);

        final List<PrimedMapping> currentPriming = got.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getAppResponses()).hasSize(2);
        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses()).containsExactly(zombiePriming.getAppResponse(), zombiePriming.getAppResponse());
    }

    @Test
    public void getResponseReturnsOptionalOfAppResponseIfPrimingExistsForAppRequest() throws Exception {
        primedMappings.add(new PrimedMapping(zombiePriming.getAppRequest(), new ArrayList<AppResponse>(){{
            add(zombiePriming.getAppResponse());
        }}));

        final Optional<AppResponse> got = primingContext.getResponse(zombiePriming.getAppRequest());

        assertThat(got.isPresent()).isTrue();
        assertThat(got.get()).isEqualTo(zombiePriming.getAppResponse());
    }

    @Test
    public void getResponseRemovesFirstAppResponseFromPrimingIfMultipleResponsesExistForPrimingOfAppRequest() throws Exception {
        primedMappings.add(new PrimedMapping(zombiePriming.getAppRequest(), new ArrayList<AppResponse>(){{
            add(zombiePriming.getAppResponse());
            add(zombiePriming.getAppResponse());
        }}));

        primingContext.getResponse(zombiePriming.getAppRequest());

        final List<PrimedMapping> currentPriming = primingContext.getCurrentPriming();

        assertThat(currentPriming).hasSize(1);

        final PrimedMapping primedMapping = currentPriming.get(0);

        assertThat(primedMapping.getAppResponses()).hasSize(1);
        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses()).containsExactly(zombiePriming.getAppResponse());
    }

    @Test
    public void getResponseRemovesPrimingFromContextIfSingleResponseExistsForPrimingOfAppRequest() throws Exception {
        primedMappings.add(new PrimedMapping(zombiePriming.getAppRequest(), new ArrayList<AppResponse>(){{
            add(zombiePriming.getAppResponse());
        }}));

        primingContext.getResponse(zombiePriming.getAppRequest());

        final List<PrimedMapping> currentPriming = primingContext.getCurrentPriming();

        assertThat(currentPriming).hasSize(0);
    }

    @Test
    public void getResponseReturnsEmptyOptionalIfPrimingDoesNotExistForAppRequest() throws Exception {
        final Optional<AppResponse> got = primingContext.getResponse(zombiePriming.getAppRequest());

        assertThat(got.isPresent()).isFalse();
    }

    @Test
    public void clearResetsTheCurrentPriming() throws Exception {
        primedMappings.add(new PrimedMapping(zombiePriming.getAppRequest(), new ArrayList<AppResponse>(){{
            add(zombiePriming.getAppResponse());
        }}));

        assertThat(primingContext.getCurrentPriming()).hasSize(1);

        primingContext.clear();

        assertThat(primingContext.getCurrentPriming()).hasSize(0);
    }
}