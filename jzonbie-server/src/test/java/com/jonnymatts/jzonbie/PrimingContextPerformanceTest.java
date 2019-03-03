package com.jonnymatts.jzonbie;

import com.flextrade.jfixture.JFixture;
import com.jonnymatts.jzonbie.priming.PrimingContext;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.jonnymatts.jzonbie.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;

@Ignore("Run this only if you are changing the priming context")
public class PrimingContextPerformanceTest {

    private JFixture fixture;
    private PrimingContext primingContext;
    private List<ZombiePriming> primings;
    private Set<Integer> indices;

    @Before
    public void setup() {
        fixture = new JFixture();
        primingContext = new PrimingContext();
        System.out.println("Fixturing data...");
        primings = IntStream.range(0, 100_000).boxed().map(
                i -> new ZombiePriming(
                        get("/path")
                                .withBody(objectBody(fixture.collections().createMap(String.class, String.class, 50)))
                                .withHeader("key1", fixtureString())
                                .withHeader("key2", fixtureString())
                                .withHeader("key3", fixtureString())
                                .withHeader("key4", fixtureString())
                                .withQueryParam("param1", fixtureString())
                                .withQueryParam("param2", fixtureString())
                                .withQueryParam("param3", fixtureString())
                                .build(),

                        AppResponse.builder(fixture.create(Integer.class)).build()
                )
        ).collect(Collectors.toList());
        System.out.println("... done.");
        indices = new HashSet<>();
        while (indices.size() < 1000) {
            indices.add(RandomUtils.nextInt(0, primings.size()));
        }
    }

    private String fixtureString() {
        return fixture.create(String.class);
    }

    @Test                 //  primed,   got
    //                        30_000, 1,000
    // naive,loop                100,    10
    // map, linear insert        100,     6
    // map, get on insert        0.1, 0.005
    public void getResponseFromPrimedContext() {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        primings.forEach(primingContext::add);
        stopWatch.stop();
        long time = stopWatch.getTime();
        System.out.println(time + " ms elapsed inserting " + primings.size() + " primings");

        stopWatch.reset();
        stopWatch.start();
        for (int i : indices) {
            final AppRequest request = primings.get(i).getRequest();
            primingContext.getResponse(request).get();
        }
        time = stopWatch.getTime();
        System.out.println(time + " ms elapsed getting " + indices.size() + " requests");
    }
}