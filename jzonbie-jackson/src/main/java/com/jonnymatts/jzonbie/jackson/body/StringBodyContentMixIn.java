package com.jonnymatts.jzonbie.jackson.body;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jonnymatts.jzonbie.body.StringBodyContent;

public abstract class StringBodyContentMixIn {

    @JsonCreator
    private static StringBodyContent stringBody(String content) {return null;}
}