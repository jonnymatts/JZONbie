package com.jonnymatts.jzonbie.model;

import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.google.common.collect.Multimap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;
import java.util.Map.Entry;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PrimedMappingFactoryTest {

    public @Rule FixtureRule fixtureRule = FixtureRule.initFixtures();

    private @Mock Multimap<ZombieRequest, ZombieResponse> primingContext;

    private @Fixture Map<ZombieRequest, Collection<ZombieResponse>> primingContextMap;

    private final PrimedMappingFactory primedMappingFactory = new PrimedMappingFactory();

    @Test
    public void createReturnsListOfPrimedRequests() throws Exception {
        when(primingContext.asMap()).thenReturn(primingContextMap);

        final List<PrimedMapping> primedRequests = primedMappingFactory.create(primingContext);

        final ArrayList<Entry<ZombieRequest, Collection<ZombieResponse>>> entries = new ArrayList<>(primingContextMap.entrySet());

        for(int i = 0; i < primedRequests.size(); i++) {
            final Entry<ZombieRequest, Collection<ZombieResponse>> entry = entries.get(i);
            final PrimedMapping primedRequest = primedRequests.get(i);

            assertThat(primedRequest.getZombieRequest()).isEqualTo(entry.getKey());
            assertThat(primedRequest.getZombieResponses()).isEqualTo(entry.getValue());
        }
    }

    @Test
    public void createReturnsEmptyListIfPrimingContextIsEmpty() throws Exception {
        when(primingContext.asMap()).thenReturn(emptyMap());

        final List<PrimedMapping> primedRequests = primedMappingFactory.create(primingContext);

        assertThat(primedRequests).isEmpty();
    }
}