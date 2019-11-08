package com.jonnymatts.jzonbie.junit;

import com.jonnymatts.jzonbie.Jzonbie;
import org.assertj.core.util.Files;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;
import static com.jonnymatts.jzonbie.defaults.StandardPriming.priming;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;

class CustomJzonbie extends Jzonbie {
    CustomJzonbie() {
        super(
                options()
                        .withPriming(priming(get("/"), ok()))
                        .withHomePath(Files.temporaryFolder().getPath())
        );
    }

    String customMethod() {
        return "hello!";
    }
}