package com.jonnymatts.jzonbie.responses;

import com.jonnymatts.jzonbie.responses.DefaultAppResponse.DynamicDefaultAppResponse;
import com.jonnymatts.jzonbie.responses.DefaultAppResponse.StaticDefaultAppResponse;
import org.junit.Test;

import java.util.Iterator;

import static com.jonnymatts.jzonbie.responses.AppResponse.notFound;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.responses.DefaultAppResponse.DynamicDefaultAppResponse.dynamicDefault;
import static com.jonnymatts.jzonbie.responses.DefaultAppResponse.StaticDefaultAppResponse.staticDefault;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class DefaultAppResponseTest {

    private final AppResponse response1 = ok().build();
    private final AppResponse response2 = notFound().build();

    @Test
    public void getResponseFromStaticDefaultResponseReturnsResponse() throws Exception {
        final StaticDefaultAppResponse defaultResponse = staticDefault(response1);

        final AppResponse got = defaultResponse.getResponse();

        assertThat(got).isEqualTo(response1);
    }

    @Test
    public void getResponseFromDynamicDefaultResponseReturnsResponseFromSupplier() throws Exception {
        final Iterator<AppResponse> appResponses = asList(response1, response2).iterator();

        final DynamicDefaultAppResponse defaultResponse = dynamicDefault(appResponses::next);

        final AppResponse got1 = defaultResponse.getResponse();

        assertThat(got1).isEqualTo(response1);

        final AppResponse got2 = defaultResponse.getResponse();

        assertThat(got2).isEqualTo(response2);
    }
}