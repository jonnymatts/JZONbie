package com.jonnymatts.jzonbie.jackson.body;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jonnymatts.jzonbie.body.ArrayBodyContent;

import java.util.List;

public abstract class ArrayBodyContentMixIn {

    @JsonCreator
    private static ArrayBodyContent arrayBody(List<?> content) {return null;}
}