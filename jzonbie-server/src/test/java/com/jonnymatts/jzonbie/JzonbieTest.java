package com.jonnymatts.jzonbie;

import com.jonnymatts.jzonbie.client.JzonbieHttpClient;
import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.response.DefaultAppResponse;
import com.jonnymatts.jzonbie.response.DefaultAppResponse.DynamicDefaultAppResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;
import static com.jonnymatts.jzonbie.model.content.StringBodyContent.stringBody;
import static com.jonnymatts.jzonbie.response.DefaultAppResponse.DynamicDefaultAppResponse.dynamicDefault;
import static com.jonnymatts.jzonbie.response.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class JzonbieTest {

    private Jzonbie jzonbie;

    @Before
    public void setUp() throws Exception {
        jzonbie = new Jzonbie();
    }

    @After
    public void tearDown() throws Exception {
        jzonbie.stop();
    }

    @Test
    public void jzonbieCanBePrimed() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.primeZombie(
                AppRequest.builder("GET", "/").build(),
                AppResponse.builder(200).withBody(singletonMap("key", "val")).build()
        );

        final JzonbieHttpClient jzonbieHttpClient = new JzonbieHttpClient("http://localhost:" + jzonbie.getPort());

        final List<PrimedMapping> got = jzonbieHttpClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());
    }

    @Test
    public void jzonbieCanBePrimedWithStringBodyContent() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.primeZombie(
                AppRequest.builder("POST", "/").withBody("<jzonbie>message</jzonbie>").build(),
                AppResponse.builder(200).withBody("<response>message</response>").build()
        );

        final JzonbieHttpClient jzonbieHttpClient = new JzonbieHttpClient("http://localhost:" + jzonbie.getPort());

        final List<PrimedMapping> got = jzonbieHttpClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());
    }

    @Test
    public void jzonbieCanBePrimedWithListBodyContent() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.primeZombie(
                AppRequest.builder("POST", "/").withBody(singletonList("request")).build(),
                AppResponse.builder(200).withBody(singletonList("response")).build()
        );

        final JzonbieHttpClient jzonbieHttpClient = new JzonbieHttpClient("http://localhost:" + jzonbie.getPort());

        final List<PrimedMapping> got = jzonbieHttpClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());
    }

    @Test
    public void jzonbieCanBePrimedWithJsonStringListBodyContent() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.primeZombie(
                AppRequest.builder("POST", "/").withBody(stringBody("request")).build(),
                AppResponse.builder(200).withBody(stringBody("response")).build()
        );

        final JzonbieHttpClient jzonbieHttpClient = new JzonbieHttpClient("http://localhost:" + jzonbie.getPort());

        final List<PrimedMapping> got = jzonbieHttpClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());
    }


    @Test
    public void jzonbieCanBePrimedWithNumberBodyContent() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.primeZombie(
                AppRequest.builder("POST", "/").withBody(1).build(),
                AppResponse.builder(200).withBody(2).build()
        );

        final JzonbieHttpClient jzonbieHttpClient = new JzonbieHttpClient("http://localhost:" + jzonbie.getPort());

        final List<PrimedMapping> got = jzonbieHttpClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());
    }

    @Test
    public void jzonbieCanBePrimedForStaticDefault() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.primeZombieForDefault(
                AppRequest.builder("GET", "/").build(),
                staticDefault(AppResponse.builder(200).withBody(singletonMap("key", "val")).build())
        );

        final JzonbieHttpClient jzonbieHttpClient = new JzonbieHttpClient("http://localhost:" + jzonbie.getPort());

        final List<PrimedMapping> got = jzonbieHttpClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getDefault().map(DefaultAppResponse::getResponse)).contains(zombiePriming.getAppResponse());
    }

    @Test
    public void jzonbieCanBePrimedForDynamicDefault() throws Exception {
        final DynamicDefaultAppResponse defaultResponse = dynamicDefault(() -> AppResponse.builder(200).withBody(singletonMap("key", "val")).build());
        final ZombiePriming zombiePriming = jzonbie.primeZombieForDefault(
                AppRequest.builder("GET", "/").build(),
                defaultResponse
        );

        final List<PrimedMapping> got = jzonbie.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());

        assertThat(primedMapping.getAppResponses().getDefault()).contains(defaultResponse);
    }

    @Test
    public void zombieHeaderNameCanBeSet() throws Exception {
        final String zombieHeaderName = "jzonbie";
        final Jzonbie jzonbieWithZombieHeaderNameSet = new Jzonbie(options().withZombieHeaderName(zombieHeaderName));

        final ZombiePriming zombiePriming = jzonbieWithZombieHeaderNameSet.primeZombie(
                AppRequest.builder("GET", "/").build(),
                AppResponse.builder(200).withBody(singletonMap("key", "val")).build()
        );

        final JzonbieHttpClient jzonbieHttpClient = new JzonbieHttpClient(
                "http://localhost:" + jzonbieWithZombieHeaderNameSet.getPort(),
                zombieHeaderName
        );

        final List<PrimedMapping> got = jzonbieHttpClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getEntries()).containsOnly(zombiePriming.getAppResponse());

        jzonbieWithZombieHeaderNameSet.stop();
    }
}