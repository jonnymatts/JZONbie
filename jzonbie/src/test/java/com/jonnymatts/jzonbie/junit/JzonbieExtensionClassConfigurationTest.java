package com.jonnymatts.jzonbie.junit;

import com.jonnymatts.jzonbie.junit.JzonbieExtension.JzonbieConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(JzonbieExtension.class)
@JzonbieConfiguration(CustomJzonbie.class)
class JzonbieExtensionClassConfigurationTest {

    private static CustomJzonbie lastJzonbie;
    private static CustomJzonbie currentJzonbie;

    @RepeatedTest(10)
    void jzonbieIsInjectedThroughParameters(CustomJzonbie jzonbie) {
        currentJzonbie = jzonbie;
        jzonbie.prime(get("/"), ok());

        assertThat(jzonbie.getCurrentPriming()).hasSize(1);
    }

    @AfterEach
    void tearDown() {
        if(lastJzonbie != null && currentJzonbie != null) {
            assertThat(lastJzonbie).isSameAs(currentJzonbie);
            assertThat(currentJzonbie.getCurrentPriming().get(0).getResponses().getPrimed()).hasSize(2);
        }
        lastJzonbie = currentJzonbie;
        currentJzonbie = null;
    }
}