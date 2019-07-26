package com.jonnymatts.jzonbie.junit;

import org.junit.Rule;
import org.junit.Test;

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
}