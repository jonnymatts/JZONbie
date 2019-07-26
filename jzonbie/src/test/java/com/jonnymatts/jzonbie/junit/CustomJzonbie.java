package com.jonnymatts.jzonbie.junit;

import com.jonnymatts.jzonbie.Jzonbie;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;
import static com.jonnymatts.jzonbie.defaults.StandardPriming.priming;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;

class CustomJzonbie extends Jzonbie {
    CustomJzonbie() {
        super(
                options()
                        .withPriming(priming(get("/").build(), ok().build()))
        );
    }

    String customMethod() {
        return "hello!";
    }
}