package com.jonnymatts.jzonbie.pippo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.google.common.base.Stopwatch;
import com.jonnymatts.jzonbie.Body;
import com.jonnymatts.jzonbie.JzonbieOptions;
import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.body.LiteralBodyContent;
import com.jonnymatts.jzonbie.requests.AppRequestHandler;
import com.jonnymatts.jzonbie.requests.PrimingNotFoundException;
import com.jonnymatts.jzonbie.requests.RequestHandler;
import com.jonnymatts.jzonbie.requests.ZombieRequestHandler;
import com.jonnymatts.jzonbie.responses.CurrentPrimingFileResponseFactory.FileResponse;
import com.jonnymatts.jzonbie.responses.ErrorResponse;
import com.jonnymatts.jzonbie.responses.PrimingNotFoundErrorResponse;
import com.jonnymatts.jzonbie.templating.TransformationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.route.RouteContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
    private final List<JzonbieRoute> additionalRoutes;
    private final Handlebars handlebars;

    public PippoApplication(JzonbieOptions options,
                            AppRequestHandler appRequestHandler,
                            ZombieRequestHandler zombieRequestHandler,
                            ObjectMapper objectMapper,
                            List<JzonbieRoute> additionalRoutes,
                            Handlebars handlebars) {
        this.appRequestHandler = appRequestHandler;
        this.zombieRequestHandler = zombieRequestHandler;
        this.objectMapper = objectMapper;
        this.zombieHeaderName = options.getZombieHeaderName();
        this.additionalRoutes = additionalRoutes;
        this.handlebars = handlebars;
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
            final Response response = requestHandler.handle(pippoRequest);
            if(response instanceof FileResponse) {
                final FileResponse fileResponse = (FileResponse) response;
                pippoResponse.contentType(APPLICATION_JSON);
                pippoResponse.file(fileResponse.getFileName(), new ByteArrayInputStream(fileResponse.getContents().getBytes()));
            } else if(response.isTemplated()) {
                final TransformationContext transformationContext = new TransformationContext(pippoRequest);
                final Map<String, String> transformedHeaders = transformHeaders(transformationContext, response.getHeaders());
                primeResponse(pippoResponse, response.getStatusCode(), transformedHeaders);
                sleepIfNecessary(response);
                final String bodyString = getBodyString(response.getBody());
                final String transformedBodyString = transformResponseBody(transformationContext, bodyString);
                send(routeContext, transformedBodyString);
            } else {
                primeResponse(pippoResponse, response.getStatusCode(), response.getHeaders());
                sleepIfNecessary(response);
                final String bodyString = getBodyString(response.getBody());
                send(routeContext, bodyString);
            }
        } catch (PrimingNotFoundException e) {
            LOGGER.error("Priming not found for request {}", e.getRequest());
            sendErrorResponse(routeContext, SC_NOT_FOUND, new PrimingNotFoundErrorResponse(e.getRequest()));
        } catch (Exception e) {
            LOGGER.error("Exception occurred: " + e.getClass().getSimpleName(), e);
            sendErrorResponse(routeContext, SC_INTERNAL_SERVER_ERROR, new ErrorResponse(format("Error occurred: %s - %s", e.getClass().getName(), e.getMessage())));
        } finally {
            stopwatch.stop();
            LOGGER.debug("Handled request {} in {} ms", pippoRequest, stopwatch.elapsed(MILLISECONDS));
        }
    }

    private void send(RouteContext routeContext, String bodyString) {
        if(bodyString ==  null) {
            routeContext.getResponse().commit();
        } else {
            routeContext.send(bodyString);
        }
    }

    private String getBodyString(Body<?> body) throws JsonProcessingException {
        if(body == null) return null;
        if(body instanceof LiteralBodyContent) return ((LiteralBodyContent) body).getContent();
        return objectMapper.writeValueAsString(body.getContent());
    }

    private void sleepIfNecessary(Response<?> response) {
        response.getDelay().ifPresent(d -> {
            try {
                Thread.sleep(d.toMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Map<String, String> transformHeaders(TransformationContext transformationContext, Map<String, String> headers) throws IOException {
        if(headers == null) return null;
        final Map<String, String> transformedHeaders = new HashMap<>();
        for(Entry<String, String> header : headers.entrySet()) {
            final Template template = handlebars.compileInline(header.getValue());
            final String transformedValue = template.apply(transformationContext);
            transformedHeaders.put(header.getKey(), transformedValue);
        }
        return transformedHeaders;
    }

    private String transformResponseBody(TransformationContext transformationContext, String bodyString) throws IOException {
        final Template template = handlebars.compileInline(bodyString);
        return template.apply(transformationContext);
    }

    private void primeResponse(ro.pippo.core.Response response, int statusCode, Map<String, String> headers) throws IOException {
        response.status(statusCode);
        if(headers != null) {
            headers.forEach(response::header);
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