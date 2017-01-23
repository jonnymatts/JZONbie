package com.jonnymatts.jzonbie.model;

import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;
import com.jonnymatts.jzonbie.util.Deserializer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Request;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ZombieRequestFactoryTest {

    @Rule public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Mock private Deserializer deserializer;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private Request request;

    @Fixture private ZombieRequest zombieRequest;

    @Fixture private String path;

    @Fixture private String requestMethod;

    @Fixture private String requestBody;

    private final Map<String, String[]> queryParams = singletonMap("qVar", new String[]{"qVal1", "qVal2"});
    private final Map<String, Object> bodyMap = singletonMap("var", "val");

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
            put("headers", singletonMap("hVar", "hVal"));
            put("queryParams", singletonMap("qVar", asList("qVal1", "qVal2")));
        }};

        when(request.pathInfo()).thenReturn(path);
        when(request.requestMethod()).thenReturn(requestMethod);
        when(request.headers()).thenReturn(singleton("hVar"));
        when(request.headers("hVar")).thenReturn("hVal");
        when(request.queryMap().toMap()).thenReturn(singletonMap("qVar", new String[]{"qVal1", "qVal2"}));
        when(request.body()).thenReturn(requestBody);

        when(deserializer.deserialize(requestBody)).thenReturn(bodyMap);
        when(deserializer.deserialize(expectedMap, ZombieRequest.class)).thenReturn(zombieRequest);

        final ZombieRequest got = zombieRequestFactory.create(request);

        assertThat(got).isEqualTo(zombieRequest);
    }
}