package com.jonnymatts.jzonbie.util;

import com.flextrade.jfixture.FixtureAnnotations;
import com.flextrade.jfixture.annotations.Fixture;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.AppResponseBuilder;

import java.util.Map;

public class AppResponseBuilderUtil {

    @Fixture private int statusCode;
    @Fixture private Map<String, Object> body;
    @Fixture private Map<String, String> headers;
    private final AppResponseBuilder builder;

    private AppResponseBuilderUtil() {
        FixtureAnnotations.initFixtures(this);

        builder = AppResponse.builder(statusCode).withBody(body);
        headers.entrySet().forEach(h -> builder.withHeader(h.getKey(), h.getValue()));
    }

    public static AppResponse getFixturedAppResponse() {
        return new AppResponseBuilderUtil().builder.build();
    }
}
