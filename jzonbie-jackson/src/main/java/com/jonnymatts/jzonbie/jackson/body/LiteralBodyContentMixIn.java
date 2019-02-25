package com.jonnymatts.jzonbie.jackson.body;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jonnymatts.jzonbie.body.LiteralBodyContent;

public abstract class LiteralBodyContentMixIn {

    @JsonCreator
    public static LiteralBodyContent literalBody(Object content) {return null;}
}