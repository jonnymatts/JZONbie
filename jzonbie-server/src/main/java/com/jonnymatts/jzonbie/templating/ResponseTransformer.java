package com.jonnymatts.jzonbie.templating;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class ResponseTransformer {
    private final Handlebars handlebars;

    public ResponseTransformer(Handlebars handlebars) {
        this.handlebars = handlebars;
    }

    public Map<String, String> transformHeaders(TransformationContext transformationContext, Map<String, String> headers) {
        if(headers == null) return null;
        final Map<String, String> transformedHeaders = new HashMap<>();

        for(Map.Entry<String, String> header : headers.entrySet()) {
            final String transformedValue = transformValue(transformationContext, header.getValue());
            transformedHeaders.put(header.getKey(), transformedValue);
        }
        return transformedHeaders;
    }

    public String transformBody(TransformationContext transformationContext, String bodyString) {
        return transformValue(transformationContext, bodyString);
    }

    private String transformValue(TransformationContext transformationContext, String value) {
        try {
            final Template template = handlebars.compileInline(value);
            return template.apply(transformationContext);
        } catch (Exception e) {
            throw new TransformResponseException(format("Could not transform: %s", value), e);
        }
    }
}