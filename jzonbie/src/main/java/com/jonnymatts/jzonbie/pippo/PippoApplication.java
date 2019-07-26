package com.jonnymatts.jzonbie.pippo;

import com.google.common.base.Stopwatch;
import com.jonnymatts.jzonbie.requests.AppRequestHandler;
import com.jonnymatts.jzonbie.requests.RequestHandler;
import com.jonnymatts.jzonbie.requests.ZombieRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.route.RouteContext;

import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class PippoApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(PippoApplication.class);

    private final String zombieHeaderName;
    private final List<JzonbieRoute> additionalRoutes;
    private final AppRequestHandler appRequestHandler;
    private final ZombieRequestHandler zombieRequestHandler;
    private final PippoResponder pippoResponder;

    public PippoApplication(String zombieHeaderName,
                            List<JzonbieRoute> additionalRoutes,
                            AppRequestHandler appRequestHandler,
                            ZombieRequestHandler zombieRequestHandler,
                            PippoResponder pippoResponder) {
        this.zombieHeaderName = zombieHeaderName;
        this.additionalRoutes = additionalRoutes;
        this.appRequestHandler = appRequestHandler;
        this.zombieRequestHandler = zombieRequestHandler;
        this.pippoResponder = pippoResponder;
    }

    @Override
    protected void onInit() {
        additionalRoutes.forEach(route -> route.accept(this));
        ANY(".*", this::handleRequest);
    }

    private void handleRequest(RouteContext routeContext) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final PippoRequest pippoRequest = new PippoRequest(routeContext.getRequest());
        final ro.pippo.core.Response pippoResponse = routeContext.getResponse();

        final String zombieHeader = pippoRequest.getHeaders().get(zombieHeaderName);

        final RequestHandler requestHandler = zombieHeader != null ?
                zombieRequestHandler : appRequestHandler;

        pippoResponder.send(pippoResponse, pippoRequest, () -> requestHandler.handle(pippoRequest));

        stopwatch.stop();
        LOGGER.debug("Handled request {} in {} ms", pippoRequest, stopwatch.elapsed(MILLISECONDS));
    }
}