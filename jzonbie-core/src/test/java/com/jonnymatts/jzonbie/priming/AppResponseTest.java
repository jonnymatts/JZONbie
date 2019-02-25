package com.jonnymatts.jzonbie.priming;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Theories.class)
public class AppResponseTest {

    @DataPoints("staticBuilders")
    public static StaticBuilderData[] staticBuilders = new StaticBuilderData[]{
            new StaticBuilderData("OK", 200, AppResponse::ok),
            new StaticBuilderData("CREATED", 201, AppResponse::created),
            new StaticBuilderData("ACCEPTED", 202, AppResponse::accepted),
            new StaticBuilderData("NO_CONTENT", 204, AppResponse::noContent),
            new StaticBuilderData("BAD_REQUEST", 400, AppResponse::badRequest),
            new StaticBuilderData("UNAUTHORIZED", 401, AppResponse::unauthorized),
            new StaticBuilderData("FORBIDDEN", 403, AppResponse::forbidden),
            new StaticBuilderData("NOT_FOUND", 404, AppResponse::notFound),
            new StaticBuilderData("METHOD_NOT_ALLOWED", 405, AppResponse::methodNotAllowed),
            new StaticBuilderData("CONFLICT", 409, AppResponse::conflict),
            new StaticBuilderData("INTERNAL_ERROR", 500, AppResponse::internalServerError),
            new StaticBuilderData("SERVICE_UNAVAILABLE", 503, AppResponse::serviceUnavailable),
            new StaticBuilderData("GATEWAY_TIMEOUT", 504, AppResponse::gatewayTimeout),
    };

    @Theory
    public void staticBuildersCreatesResponse(@FromDataPoints("staticBuilders") StaticBuilderData data) {
        System.out.println("Testing static builder: " + data.status);

        final AppResponse request = data.builder.get().build();

        assertThat(request.getStatusCode()).isEqualTo(data.statusCode);
    }

    private static class StaticBuilderData {
        private final String status;
        private final int statusCode;
        private final Supplier<AppResponseBuilder> builder;

        private StaticBuilderData(String status, int statusCode, Supplier<AppResponseBuilder> builder) {
            this.status = status;
            this.statusCode = statusCode;
            this.builder = builder;
        }
    }
}