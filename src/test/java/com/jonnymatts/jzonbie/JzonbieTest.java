package com.jonnymatts.jzonbie;

import com.jonnymatts.jzonbie.client.JzonbieHttpClient;
import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
    public void jzonbieCanBePrimedForDefault() throws Exception {
        final ZombiePriming zombiePriming = jzonbie.primeZombieForDefault(
                AppRequest.builder("GET", "/").build(),
                AppResponse.builder(200).withBody(singletonMap("key", "val")).build()
        );

        final JzonbieHttpClient jzonbieHttpClient = new JzonbieHttpClient("http://localhost:" + jzonbie.getPort());

        final List<PrimedMapping> got = jzonbieHttpClient.getCurrentPriming();

        assertThat(got).hasSize(1);

        final PrimedMapping primedMapping = got.get(0);

        assertThat(primedMapping.getAppRequest()).isEqualTo(zombiePriming.getAppRequest());
        assertThat(primedMapping.getAppResponses().getDefault()).contains(zombiePriming.getAppResponse());
    }
}