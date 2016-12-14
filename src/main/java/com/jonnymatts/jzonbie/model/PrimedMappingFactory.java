package com.jonnymatts.jzonbie.model;

import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class PrimedMappingFactory {
    public List<PrimedMapping> create(Multimap<ZombieRequest, ZombieResponse> primingContext) {
        final Map<ZombieRequest, Collection<ZombieResponse>> map = primingContext.asMap();
        return map.entrySet().stream()
                .map(entry -> new PrimedMapping(entry.getKey(), new ArrayList<ZombieResponse>(entry.getValue())))
                .collect(toList());
    }
}