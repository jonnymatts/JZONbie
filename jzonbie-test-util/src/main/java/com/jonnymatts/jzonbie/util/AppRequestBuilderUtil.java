package com.jonnymatts.jzonbie.util;

import com.flextrade.jfixture.FixtureAnnotations;
import com.flextrade.jfixture.annotations.Fixture;
import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppRequestBuilder;

import java.util.List;
import java.util.Map;

public class AppRequestBuilderUtil {

    @Fixture private String path;
    @Fixture private String method;
    @Fixture private Map<String, Object> body;
    @Fixture private Map<String, String> headers;
    @Fixture private Map<String, List<String>> queryParams;
    private final AppRequestBuilder builder;

    private AppRequestBuilderUtil() {
        FixtureAnnotations.initFixtures(this);

        builder = AppRequest.builder(method, path).withBody(body);
        headers.entrySet().forEach(h -> builder.withHeader(h.getKey(), h.getValue()));
        queryParams.entrySet().forEach(p -> p.getValue().forEach(value -> builder.withQueryParam(p.getKey(), value)));
    }

    public static AppRequest getFixturedAppRequest() {
        return new AppRequestBuilderUtil().builder.build();
    }
}
