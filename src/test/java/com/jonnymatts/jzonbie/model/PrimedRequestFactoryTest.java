package com.jonnymatts.jzonbie.model;

import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.util.Deserializer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PrimedRequestFactoryTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Mock private Deserializer deserializer;

    @Mock private Request request;

    @Fixture private PrimedRequest primedRequest;

    @Fixture private String path;

    @Fixture private String requestMethod;

    private PrimedRequestFactory primedRequestFactory;

    @Before
    public void setUp() throws Exception {
        primedRequestFactory = new PrimedRequestFactory(deserializer);
    }

    @Test
    public void createForRequest() throws Exception {
        when(deserializer.deserialize(anyMap(), eq(PrimedRequest.class))).thenReturn(primedRequest);

        final PrimedRequest got = primedRequestFactory.create(request);

        assertThat(got).isEqualTo(primedRequest);
    }
}