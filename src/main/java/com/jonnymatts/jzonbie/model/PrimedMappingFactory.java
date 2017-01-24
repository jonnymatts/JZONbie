package com.jonnymatts.jzonbie.model;

import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class PrimedMappingFactory {
    public List<PrimedMapping> create(Multimap<AppRequest, AppResponse> primingContext) {
        final Map<AppRequest, Collection<AppResponse>> map = primingContext.asMap();
        return map.entrySet().stream()
                .map(entry -> new PrimedMapping(entry.getKey(), new ArrayList<AppResponse>(entry.getValue())))
                .collect(toList());
    }
}