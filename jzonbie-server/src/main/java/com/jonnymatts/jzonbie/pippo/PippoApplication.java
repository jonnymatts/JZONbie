package com.jonnymatts.jzonbie.pippo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.jonnymatts.jzonbie.JzonbieOptions;
import com.jonnymatts.jzonbie.model.content.BodyContent;
import com.jonnymatts.jzonbie.model.content.LiteralBodyContent;
import com.jonnymatts.jzonbie.requests.AppRequestHandler;
import com.jonnymatts.jzonbie.requests.PrimingNotFoundException;
import com.jonnymatts.jzonbie.requests.RequestHandler;
import com.jonnymatts.jzonbie.requests.ZombieRequestHandler;
import com.jonnymatts.jzonbie.response.CurrentPrimingFileResponseFactory.FileResponse;
import com.jonnymatts.jzonbie.response.ErrorResponse;
import com.jonnymatts.jzonbie.response.PrimingNotFoundErrorResponse;
import com.jonnymatts.jzonbie.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.route.RouteContext;

import java.io.ByteArrayInputStream;
import java.util.Map;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static ro.pippo.core.HttpConstants.ContentType.APPLICATION_JSON;

public class PippoApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(PippoApplication.class);

    private final AppRequestHandler appRequestHandler;
    private final ZombieRequestHandler zombieRequestHandler;
    private final ObjectMapper objectMapper;
    private final String zombieHeaderName;

    public PippoApplication(JzonbieOptions options,
                            AppRequestHandler appRequestHandler,
                            ZombieRequestHandler zombieRequestHandler,
                            ObjectMapper objectMapper) {

        this.appRequestHandler = appRequestHandler;
        this.zombieRequestHandler = zombieRequestHandler;
        this.objectMapper = objectMapper;
        this.zombieHeaderName = options.getZombieHeaderName();
    }

    @Override
    protected void onInit() {
        ALL(".*", this::handleRequest);
    }

    private void handleRequest(RouteContext routeContext) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final PippoRequest pippoRequest = new PippoRequest(routeContext.getRequest());
        final ro.pippo.core.Response pippoResponse = routeContext.getResponse();

        final String zombieHeader = pippoRequest.getHeaders().get(zombieHeaderName);

        final RequestHandler requestHandler = zombieHeader != null ?
                zombieRequestHandler : appRequestHandler;

        try {
            final Response response = requestHandler.handle(pippoRequest);
            if(response instanceof FileResponse) {
                final FileResponse fileResponse = (FileResponse) response;
                pippoResponse.contentType(APPLICATION_JSON);
                pippoResponse.file(fileResponse.getFileName(), new ByteArrayInputStream(fileResponse.getContents().getBytes()));
            } else {
                primeResponse(pippoResponse, response);

                response.getDelay().ifPresent(d -> {
                    try {
                        Thread.sleep(d.toMillis());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

                final Object body = response.getBody();

                if(body instanceof LiteralBodyContent) {
                    routeContext.send(((LiteralBodyContent) body).getContent());
                } else {
                    final Object o = (body instanceof BodyContent) ? ((BodyContent) body).getContent() : body;
                    routeContext.send(objectMapper.writeValueAsString(o));
                }
            }
        } catch (PrimingNotFoundException e) {
            LOGGER.error("Priming not found", e);
            sendErrorResponse(routeContext, SC_NOT_FOUND, new PrimingNotFoundErrorResponse(e.getRequest()));
        } catch (Exception e) {
            LOGGER.error("Exception occurred: " + e.getClass().getSimpleName(), e);
            sendErrorResponse(routeContext, SC_INTERNAL_SERVER_ERROR, new ErrorResponse(format("Error occurred: %s - %s", e.getClass().getName(), e.getMessage())));
        } finally {
            stopwatch.stop();
            LOGGER.debug("Handled request {} in {} ms", pippoRequest, stopwatch.elapsed(MILLISECONDS));
        }
    }

    private void primeResponse(ro.pippo.core.Response response, Response r) throws JsonProcessingException {
        response.status(r.getStatusCode());

        final Map<String, String> headers = r.getHeaders();

        if(headers != null) {
            headers.entrySet().forEach(entry -> response.header(entry.getKey(), entry.getValue()));
        }
    }

    private void sendErrorResponse(RouteContext routeContext, int statusCode, ErrorResponse errorResponse) {
        final ro.pippo.core.Response pippoResponse = routeContext.getResponse();
        pippoResponse.status(statusCode);

        try {
            routeContext.json().send(objectMapper.writeValueAsString(errorResponse));
        } catch (Exception e) {
            routeContext.send(errorResponse.getMessage());
        }
    }
}