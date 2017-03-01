package com.jonnymatts.jzonbie.response;

import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.response.DefaultResponse.DynamicDefaultResponse;
import com.jonnymatts.jzonbie.response.DefaultResponse.StaticDefaultResponse;
import org.junit.Test;

import java.util.Iterator;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class DefaultAppResponseTest {

    private final AppResponse response1 = AppResponse.builder(100).build();
    private final AppResponse response2 = AppResponse.builder(200).build();

    @Test
    public void getResponseFromStaticDefaultResponseReturnsResponse() throws Exception {
        final StaticDefaultResponse<AppResponse> defaultResponse = new StaticDefaultResponse<>(response1);

        final AppResponse got = defaultResponse.getResponse();

        assertThat(got).isEqualTo(response1);
    }
    @Test
    public void isDynamicFromStaticDefaultResponseReturnsFalse() throws Exception {
        final StaticDefaultResponse<AppResponse> defaultResponse = new StaticDefaultResponse<>(response1);

        final boolean got = defaultResponse.isDynamic();

        assertThat(got).isFalse();
    }

    @Test
    public void getResponseFromDynamicDefaultResponseReturnsResponseFromSupplier() throws Exception {
        final Iterator<AppResponse> appResponses = asList(response1, response2).iterator();

        final DynamicDefaultResponse<AppResponse> defaultResponse = new DynamicDefaultResponse<>(appResponses::next);

        final AppResponse got1 = defaultResponse.getResponse();

        assertThat(got1).isEqualTo(response1);

        final AppResponse got2 = defaultResponse.getResponse();

        assertThat(got2).isEqualTo(response2);
    }
    @Test
    public void isDynamicFromDynamicDefaultResponseReturnsTrue() throws Exception {
        final DynamicDefaultResponse<AppResponse> defaultResponse = new DynamicDefaultResponse<>(() -> response1);

        final boolean got = defaultResponse.isDynamic();

        assertThat(got).isTrue();
    }
}