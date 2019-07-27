package com.jonnymatts.jzonbie.junit;

import com.jonnymatts.jzonbie.Jzonbie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.responses.AppResponse.response;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(JzonbieExtension.class)
class JzonbieExtensionTest {

    private static Jzonbie lastJzonbie;
    private static Jzonbie currentJzonbie;

    @RepeatedTest(10)
    void defaultJzonbieIsInjected(Jzonbie jzonbie) {
        currentJzonbie = jzonbie;
        jzonbie.prime(get("/"), ok());

        assertThat(jzonbie.getCurrentPriming()).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 400, 500})
    void jzonbieIsInjectedInParameterizedTest(int i, Jzonbie jzonbie) {
        jzonbie.prime(get("/"), response(i));
    }

    @AfterEach
    void tearDown() {
        if(lastJzonbie != null && currentJzonbie != null) {
            assertThat(lastJzonbie).isSameAs(currentJzonbie);
            assertThat(currentJzonbie.getCurrentPriming().get(0).getResponses().getPrimed()).hasSize(1);
        }
        lastJzonbie = currentJzonbie;
        currentJzonbie = null;
    }
}