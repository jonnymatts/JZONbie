package com.jonnymatts.jzonbie;

import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
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
public class PrimingKeyFactoryTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Mock private JsonDeserializer jsonDeserializer;

    @Mock private Request request;

    @Fixture private PrimingKey primingKey;

    @Fixture private PrimedRequest primedRequest;

    private PrimingKeyFactory primingKeyFactory;

    @Before
    public void setUp() throws Exception {
        primingKeyFactory = new PrimingKeyFactory(jsonDeserializer);
    }

    @Test
    public void createForRequest() throws Exception {
        when(request.pathInfo()).thenReturn("path");
        when(jsonDeserializer.deserialize(anyMap(), eq(PrimedRequest.class))).thenReturn(primedRequest);

        final PrimingKey primingKey = primingKeyFactory.create(request);

        assertThat(primingKey.getPath()).isEqualTo(request.pathInfo());
        assertThat(primingKey.getPrimedRequest()).isEqualTo(primedRequest);
    }
}