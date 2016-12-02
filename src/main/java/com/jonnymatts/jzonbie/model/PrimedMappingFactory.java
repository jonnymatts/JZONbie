package com.jonnymatts.jzonbie.model;

import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class PrimedMappingFactory {
    public List<PrimedMapping> create(Multimap<PrimedRequest, PrimedResponse> primingContext) {
        final Map<PrimedRequest, Collection<PrimedResponse>> map = primingContext.asMap();
        return map.entrySet().stream()
                .map(entry -> new PrimedMapping(entry.getKey(), new ArrayList<PrimedResponse>(entry.getValue())))
                .collect(toList());
    }
}