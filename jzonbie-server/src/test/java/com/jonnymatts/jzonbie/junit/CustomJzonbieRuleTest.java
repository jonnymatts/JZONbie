package com.jonnymatts.jzonbie.junit;

import com.jonnymatts.jzonbie.Jzonbie;
import org.junit.Rule;
import org.junit.Test;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;
import static com.jonnymatts.jzonbie.defaults.StandardPriming.priming;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static org.assertj.core.api.Assertions.assertThat;

public class CustomJzonbieRuleTest {

    @Rule public JzonbieRule<CustomJzonbie> jzonbieRule = JzonbieRule.jzonbie(CustomJzonbie::new);

    @Test
    public void customJzonbieRuleWorksCorrectly() {
        assertThat(jzonbieRule.getCurrentPriming()).hasSize(1);
    }

    @Test
    public void getJzonbieReturnsInstanceOfCustomJzonbie() {
        final CustomJzonbie customJzonbie = jzonbieRule.getJzonbie();

        assertThat(customJzonbie.customMethod()).isEqualTo("hello!");
    }

    private static class CustomJzonbie extends Jzonbie {
        CustomJzonbie() {
            super(
                    options()
                            .withPriming(priming(get("/").build(), ok().build()))
            );
        }

        public String customMethod() {
            return "hello!";
        }
    }
}