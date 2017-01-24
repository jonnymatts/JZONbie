package com.jonnymatts.jzonbie.model;

import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.requests.Request;
import com.jonnymatts.jzonbie.util.Deserializer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ZombieRequestFactoryTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Mock private Deserializer deserializer;
    @Mock private Request request;

    @Fixture private ZombieRequest zombieRequest;
    @Fixture private String path;
    @Fixture private String requestMethod;
    @Fixture private String requestBody;

    private final Map<String, Object> bodyMap = singletonMap("var", "val");
    private final Map<String, String> headers = singletonMap("hVar", "hVal");
    private final Map<String, List<String>> queryParams = singletonMap("qVar", asList("qVal1", "qVal2"));

    private ZombieRequestFactory zombieRequestFactory;

    @Before
    public void setUp() throws Exception {
        zombieRequestFactory = new ZombieRequestFactory(deserializer);
    }

    @Test
    public void createForRequest() throws Exception {
        final HashMap<String, Object> expectedMap = new HashMap<String, Object>(){{
            put("path", path);
            put("method", requestMethod);
            put("body", bodyMap);
            put("headers", headers);
            put("queryParams", queryParams);
        }};

        when(request.getPath()).thenReturn(path);
        when(request.getMethod()).thenReturn(requestMethod);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getQueryParams()).thenReturn(this.queryParams);
        when(request.getBody()).thenReturn(requestBody);

        when(deserializer.deserialize(requestBody)).thenReturn(bodyMap);
        when(deserializer.deserialize(expectedMap, ZombieRequest.class)).thenReturn(zombieRequest);

        final ZombieRequest got = zombieRequestFactory.create(request);

        assertThat(got).isEqualTo(zombieRequest);
    }
}