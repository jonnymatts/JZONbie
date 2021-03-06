package com.jonnymatts.jzonbie.junit;

import com.jonnymatts.jzonbie.Jzonbie;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;
import static com.jonnymatts.jzonbie.defaults.StandardPriming.priming;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;

public class CustomJzonbie extends Jzonbie {
    CustomJzonbie() {
        super(
                options()
                        .withPriming(priming(get("/"), ok()))
        );
    }

    String customMethod() {
        return "hello!";
    }
}