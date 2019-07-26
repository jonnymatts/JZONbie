package com.jonnymatts.jzonbie.pippo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonnymatts.jzonbie.Body;
import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.body.LiteralBodyContent;
import com.jonnymatts.jzonbie.requests.PrimingNotFoundException;
import com.jonnymatts.jzonbie.responses.CurrentPrimingFileResponseFactory.FileResponse;
import com.jonnymatts.jzonbie.responses.ErrorResponse;
import com.jonnymatts.jzonbie.responses.PrimingNotFoundErrorResponse;
import com.jonnymatts.jzonbie.templating.ResponseTransformer;
import com.jonnymatts.jzonbie.templating.TransformationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static ro.pippo.core.HttpConstants.ContentType.APPLICATION_JSON;

public class PippoResponder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PippoApplication.class);

    private final ResponseTransformer responseTransformer;
    private final ObjectMapper objectMapper;

    public PippoResponder(ResponseTransformer responseTransformer, ObjectMapper objectMapper) {
        this.responseTransformer = responseTransformer;
        this.objectMapper = objectMapper;
    }

    public void send(ro.pippo.core.Response pippoResponse, PippoRequest pippoRequest, Supplier<Response<?>> responseSupplier) {
        try {
            final Response<?> response = responseSupplier.get();
            if(response instanceof FileResponse) {
                final FileResponse fileResponse = (FileResponse) response;
                pippoResponse.contentType(APPLICATION_JSON);
                pippoResponse.file(fileResponse.getFileName(), new ByteArrayInputStream(fileResponse.getContents().getBytes()));
            } else if(response.isTemplated()) {
                final TransformationContext transformationContext = new TransformationContext(pippoRequest);
                final Map<String, String> transformedHeaders = responseTransformer.transformHeaders(transformationContext, response.getHeaders());
                primeResponse(pippoResponse, response.getStatusCode(), transformedHeaders);
                sleepIfNecessary(response);
                final String bodyString = getBodyString(response.getBody());
                final String transformedBodyString = responseTransformer.transformBody(transformationContext, bodyString);
                send(pippoResponse, transformedBodyString);
            } else {
                primeResponse(pippoResponse, response.getStatusCode(), response.getHeaders());
                sleepIfNecessary(response);
                final String bodyString = getBodyString(response.getBody());
                send(pippoResponse, bodyString);
            }
        } catch (PrimingNotFoundException e) {
            LOGGER.error("Priming not found for request {}", e.getRequest());
            sendErrorResponse(pippoResponse, SC_NOT_FOUND, new PrimingNotFoundErrorResponse(e.getRequest()));
        } catch (Exception e) {
            LOGGER.error("Exception occurred: " + e.getClass().getSimpleName(), e);
            sendErrorResponse(pippoResponse, SC_INTERNAL_SERVER_ERROR, new ErrorResponse(format("Error occurred: %s - %s", e.getClass().getName(), e.getMessage())));
        }
    }

    private void send(ro.pippo.core.Response response, String bodyString) {
        if(bodyString ==  null) {
            response.commit();
        } else {
            response.send(bodyString);
        }
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

    private String getBodyString(Body<?> body) throws JsonProcessingException {
        if(body == null) return null;
        if(body instanceof LiteralBodyContent) return ((LiteralBodyContent) body).getContent();
        return objectMapper.writeValueAsString(body.getContent());
    }

    private void primeResponse(ro.pippo.core.Response response, int statusCode, Map<String, String> headers) throws IOException {
        response.status(statusCode);
        if(headers != null) {
            headers.forEach(response::header);
        }
    }

    private void sendErrorResponse(ro.pippo.core.Response pippoResponse, int statusCode, ErrorResponse errorResponse) {
        pippoResponse.status(statusCode);

        try {
            pippoResponse.json().send(objectMapper.writeValueAsString(errorResponse));
        } catch (Exception e) {
            pippoResponse.send(errorResponse.getMessage());
        }
    }
}