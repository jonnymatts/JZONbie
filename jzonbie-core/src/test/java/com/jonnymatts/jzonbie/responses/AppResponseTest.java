package com.jonnymatts.jzonbie.responses;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static org.assertj.core.api.Assertions.assertThat;

class AppResponseTest {

    static Stream<StaticBuilderData> staticBuilders() {
        return Stream.of(
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
            new StaticBuilderData("GATEWAY_TIMEOUT", 504, AppResponse::gatewayTimeout)
        );
    }

    @ParameterizedTest
    @MethodSource("staticBuilders")
    void staticBuildersCreatesResponse(StaticBuilderData data) {
        System.out.println("Testing static builder: " + data.status);

        final AppResponse request = data.builder.get();

        assertThat(request.getStatusCode()).isEqualTo(data.statusCode);
    }

    @Test
    void cloneResponseClonesTemplatedField() {
        final AppResponse templatedResponse = ok().templated();

        final AppResponse got = new AppResponse(templatedResponse);

        assertThat(got).isEqualTo(templatedResponse);
    }

    private static class StaticBuilderData {
        private final String status;
        private final int statusCode;
        private final Supplier<com.jonnymatts.jzonbie.responses.AppResponse> builder;

        private StaticBuilderData(String status, int statusCode, Supplier<AppResponse> builder) {
            this.status = status;
            this.statusCode = statusCode;
            this.builder = builder;
        }
    }
}