package com.jonnymatts.jzonbie.pippo;

import com.google.common.base.Stopwatch;
import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.metadata.MetaDataContext;
import com.jonnymatts.jzonbie.requests.AppRequestHandler;
import com.jonnymatts.jzonbie.requests.RequestHandler;
import com.jonnymatts.jzonbie.requests.ZombieRequestHandler;
import com.jonnymatts.jzonbie.templating.ResponseTransformer;
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
    private final ResponseTransformer responseTransformer;

    public PippoApplication(String zombieHeaderName,
                            List<JzonbieRoute> additionalRoutes,
                            AppRequestHandler appRequestHandler,
                            ZombieRequestHandler zombieRequestHandler,
                            PippoResponder pippoResponder,
                            ResponseTransformer responseTransformer) {
        this.zombieHeaderName = zombieHeaderName;
        this.additionalRoutes = additionalRoutes;
        this.appRequestHandler = appRequestHandler;
        this.zombieRequestHandler = zombieRequestHandler;
        this.pippoResponder = pippoResponder;
        this.responseTransformer = responseTransformer;
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
        try {
            pippoResponder.send(pippoResponse, getResponse(pippoRequest, requestHandler));
        } catch (Exception e) {
            pippoResponder.sendForException(pippoResponse, e);
        }
        stopwatch.stop();
        LOGGER.debug("Handled request {} in {} ms", pippoRequest, stopwatch.elapsed(MILLISECONDS));
    }

    private Response getResponse(PippoRequest request, RequestHandler requestHandler) {
        MetaDataContext context = new MetaDataContext(request);
        Response response = requestHandler.handle(request, context);
        return responseTransformer.transform(context, response);
    }
}