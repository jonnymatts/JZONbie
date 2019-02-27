package com.jonnymatts.jzonbie.jackson.body;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jonnymatts.jzonbie.body.ObjectBodyContent;

import java.util.Map;

public abstract class ObjectBodyContentMixIn {

    @JsonCreator
    public static ObjectBodyContent objectBody(Map<String, ?> content) {return null;}
}