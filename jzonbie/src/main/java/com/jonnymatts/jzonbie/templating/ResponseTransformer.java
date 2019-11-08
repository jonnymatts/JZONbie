package com.jonnymatts.jzonbie.templating;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.jonnymatts.jzonbie.Body;
import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.body.LiteralBodyContent;
import com.jonnymatts.jzonbie.metadata.MetaDataContext;
import com.jonnymatts.jzonbie.responses.AppResponse;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class ResponseTransformer {
    private final ObjectMapper objectMapper;
    private final Handlebars handlebars;

    public ResponseTransformer(ObjectMapper objectMapper, Handlebars handlebars) {
        this.objectMapper = objectMapper;
        this.handlebars = handlebars;
    }

    public Response transform(MetaDataContext metaDataContext, Response appResponse) {
        try {
            if (appResponse.isTemplated()) {
                Map<String, String> responseHeaders = appResponse.getHeaders();
                final Map<String, String> transformedHeaders = transformHeaders(metaDataContext, responseHeaders);
                final String transformedBody = transformBody(metaDataContext, getBodyString(appResponse.getBody()));
                return buildNewResponse(appResponse.getStatusCode(), transformedHeaders, transformedBody);
            } else {
                return appResponse;
            }
        } catch(TransformResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new TransformResponseException(e);
        }
    }

    private Map<String, String> transformHeaders(MetaDataContext metaDataContext, Map<String, String> headers) {
        if (headers == null) return null;
        final Map<String, String> transformedHeaders = new HashMap<>();

        for (Map.Entry<String, String> header : headers.entrySet()) {
            final String transformedValue = transformValue(metaDataContext, header.getValue());
            transformedHeaders.put(header.getKey(), transformedValue);
        }
        return transformedHeaders;
    }

    private String transformBody(MetaDataContext metaDataContext, String bodyString) {
        return transformValue(metaDataContext, bodyString);
    }

    private String transformValue(MetaDataContext metaDataContext, String value) {
        try {
            final Template template = handlebars.compileInline(value);
           return template.apply(metaDataContext.getContext());
        } catch (Exception e) {
            throw new TransformResponseException(format("Could not transform: %s", value), e);
        }
    }

    private Response buildNewResponse(int statusCode, Map<String, String> newHeaders, String transformedBody) {
        final AppResponse response = new AppResponse(statusCode);
        response.setHeaders(newHeaders);
        response.withBody(transformedBody);
        return response;
    }

    private String getBodyString(Body<?> body) throws JsonProcessingException {
        if (body == null) return null;
        if (body instanceof LiteralBodyContent) return ((LiteralBodyContent) body).getContent();
        return objectMapper.writeValueAsString(body.getContent());
    }

}