package com.jonnymatts.jzonbie;

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
public class PrimedRequestsFactoryTest {

    public @Rule FixtureRule fixtureRule = FixtureRule.initFixtures();

    private @Mock Multimap<PrimedRequest, PrimedResponse> primingContext;

    private @Fixture Map<PrimedRequest, Collection<PrimedResponse>> primingContextMap;

    private final PrimedRequestsFactory primedRequestsFactory = new PrimedRequestsFactory();

    @Test
    public void createReturnsListOfPrimedRequests() throws Exception {
        when(primingContext.asMap()).thenReturn(primingContextMap);

        final List<PrimedRequests> primedRequests = primedRequestsFactory.create(primingContext);

        final ArrayList<Entry<PrimedRequest, Collection<PrimedResponse>>> entries = new ArrayList<>(primingContextMap.entrySet());

        for(int i = 0; i < primedRequests.size(); i++) {
            final Entry<PrimedRequest, Collection<PrimedResponse>> entry = entries.get(i);
            final PrimedRequests primedRequest = primedRequests.get(i);

            assertThat(primedRequest.getPrimedRequest()).isEqualTo(entry.getKey());
            assertThat(primedRequest.getPrimedResponses()).isEqualTo(entry.getValue());
        }
    }

    @Test
    public void createReturnsEmptyListIfPrimingContextIsEmpty() throws Exception {
        when(primingContext.asMap()).thenReturn(emptyMap());

        final List<PrimedRequests> primedRequests = primedRequestsFactory.create(primingContext);

        assertThat(primedRequests).isEmpty();
    }
}