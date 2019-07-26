package com.jonnymatts.jzonbie.junit;

import com.jonnymatts.jzonbie.Jzonbie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(JzonbieExtension.class)
class JzonbieExtensionTest {

    private static Jzonbie lastJzonbie;
    private static Jzonbie currentJzonbie;

    @RepeatedTest(10)
    void jzonbieIsInjectedThroughParameters(Jzonbie jzonbie) {
        currentJzonbie = jzonbie;
        jzonbie.prime(get("/").build(), ok().build());

        assertThat(jzonbie.getCurrentPriming()).hasSize(1);
    }

    @AfterEach
    void tearDown() {
        if(lastJzonbie != null) {
            assertThat(lastJzonbie).isSameAs(currentJzonbie);
        }
        assertThat(currentJzonbie.getCurrentPriming()).hasSize(1);
        lastJzonbie = currentJzonbie;
    }
}