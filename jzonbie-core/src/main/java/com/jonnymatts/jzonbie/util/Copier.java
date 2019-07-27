package com.jonnymatts.jzonbie.util;

import com.jonnymatts.jzonbie.body.BodyContent;

import java.util.HashMap;
import java.util.Map;


public class Copier {

    public static <K, V> HashMap<K, V> copyMap(Map<K, V> map) {
        return map == null ? null : new HashMap<>(map);
    }

    public static BodyContent<?> copyBodyContent(BodyContent<?> body) {
        if(body == null) return null;
        return body.copy();
    }
}