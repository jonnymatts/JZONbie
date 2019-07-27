package com.jonnymatts.jzonbie.junit;

import org.junit.Rule;
import org.junit.Test;

import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static org.assertj.core.api.Assertions.assertThat;

public class JzonbieRuleTest {

    @Rule public JzonbieRule jzonbieRule = JzonbieRule.jzonbie();

    @Test
    public void jzonbieRuleWorksCorrectly() {
        jzonbieRule.prime(get("/"), ok());

        assertThat(jzonbieRule.getCurrentPriming()).hasSize(1);
    }
}