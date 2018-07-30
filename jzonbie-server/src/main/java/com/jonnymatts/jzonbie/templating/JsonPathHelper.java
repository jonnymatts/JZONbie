package com.jonnymatts.jzonbie.templating;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;

import static java.lang.String.format;

public class JsonPathHelper implements Helper<String> {

    @Override
    public Object apply(String context, Options options) throws IOException {
        final String jsonPathString = options.param(0);
        try {
            return JsonPath.<String>read(context, jsonPathString);
        } catch (Exception e) {
            throw new TransformResponseException(format("Could not extract '%s' from '%s'", jsonPathString, context), e);
        }
    }
}