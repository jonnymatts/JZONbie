package com.jonnymatts.jzonbie.model;

import com.jonnymatts.jzonbie.model.content.BodyContent;
import com.jonnymatts.jzonbie.response.DefaultAppResponse;
import com.jonnymatts.jzonbie.response.DefaultingQueue;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class PrimingContext {
    private Map<HeaderlessAppRequest, Map<AppRequest, DefaultingQueue>> primedMappings;

    public PrimingContext() {
        this.primedMappings = new HashMap<>();
    }

    public List<PrimedMapping> getCurrentPriming() {
        return primedMappings.entrySet().stream().map(e -> e.getValue().entrySet()).flatMap(Collection::stream)
                .map(e -> new PrimedMapping(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    synchronized public PrimingContext add(ZombiePriming zombiePriming) {
        return add(zombiePriming.getAppRequest(), zombiePriming.getAppResponse());
    }

    public PrimingContext add(AppRequest appRequest, AppResponse appResponse) {
        final DefaultingQueue responseQueue = getAppResponseQueueForAdd(appRequest);

        responseQueue.add(appResponse);

        return this;
    }

    public PrimingContext addDefault(AppRequest appRequest, DefaultAppResponse defaultAppResponse) {
        final DefaultingQueue responseQueue = getAppResponseQueueForAdd(appRequest);

        responseQueue.setDefault(defaultAppResponse);

        return this;
    }

    private DefaultingQueue getAppResponseQueueForAdd(AppRequest appRequest) {
        final HeaderlessAppRequest headerlessAppRequest = new HeaderlessAppRequest(appRequest);
        Map<AppRequest, DefaultingQueue> mappingsForHeaderlessRequest = primedMappings.get(headerlessAppRequest);

        if(mappingsForHeaderlessRequest == null) {
            mappingsForHeaderlessRequest = new HashMap<>();
            primedMappings.put(headerlessAppRequest, mappingsForHeaderlessRequest);
        }

        DefaultingQueue responseQueue = mappingsForHeaderlessRequest.get(appRequest);

        if(responseQueue == null) {
            responseQueue = new DefaultingQueue();
            mappingsForHeaderlessRequest.put(appRequest, responseQueue);
        }

        return responseQueue;
    }

    synchronized public Optional<AppResponse> getResponse(AppRequest appRequest) {
        final HeaderlessAppRequest headerlessAppRequest = new HeaderlessAppRequest(appRequest);
        final Optional<MapAppRequestAndQueue> mapAppRequestAndQueue = findMapAndQueue(appRequest);

        if (!mapAppRequestAndQueue.isPresent())
            return empty();

        return mapAppRequestAndQueue.map(m -> {
            final DefaultingQueue responseQueue = m.getQueue();
            final Map<AppRequest, DefaultingQueue> mapping = m.getMap();

            final AppResponse appResponse = responseQueue.poll();

            if(responseQueue.hasSize() == 0 && !responseQueue.getDefault().isPresent())
                mapping.remove(m.getAppRequest());

            if(mapping.isEmpty())
                primedMappings.remove(headerlessAppRequest);
            return appResponse;
        });
    }

    private Optional<MapAppRequestAndQueue> findMapAndQueue(AppRequest appRequest) {
        final HeaderlessAppRequest headerlessAppRequest = new HeaderlessAppRequest(appRequest);
        Map<AppRequest, DefaultingQueue> map = primedMappings.get(headerlessAppRequest);
        if(map == null) {
            for (Map<AppRequest, DefaultingQueue> e : primedMappings.values()) {
                final Optional<Map.Entry<AppRequest, DefaultingQueue>> entryOpt = findResponseQueueFromMapForRequest(e, appRequest);
                if(entryOpt.isPresent()) {
                    final Map.Entry<AppRequest, DefaultingQueue> entry = entryOpt.get();
                    return of(new MapAppRequestAndQueue(e, entry.getKey(), entry.getValue()));
                }
            }
        } else {
            return findResponseQueueFromMapForRequest(map, appRequest).map(q -> new MapAppRequestAndQueue(map, q.getKey(), q.getValue()));
        }
        return empty();
    }

    private static class MapAppRequestAndQueue {
        private final Map<AppRequest, DefaultingQueue> map;
        private final AppRequest appRequest;
        private final DefaultingQueue queue;

        public MapAppRequestAndQueue(Map<AppRequest, DefaultingQueue> map, AppRequest appRequest, DefaultingQueue queue) {
            this.map = map;
            this.appRequest = appRequest;
            this.queue = queue;
        }

        public Map<AppRequest, DefaultingQueue> getMap() {
            return map;
        }

        public AppRequest getAppRequest() {
            return appRequest;
        }

        public DefaultingQueue getQueue() {
            return queue;
        }
    }

    private Optional<Map.Entry<AppRequest, DefaultingQueue>> findResponseQueueFromMapForRequest(Map<AppRequest, DefaultingQueue> map, AppRequest appRequest) {
        return map.entrySet().parallelStream()
                .filter(priming -> priming.getKey().matches(appRequest))
                .findFirst();
    }

    synchronized public void clear() {
        primedMappings.clear();
    }

    private static class HeaderlessAppRequest {
        private final String path;
        private final String method;
        private final BodyContent body;
        private final Map<String, List<String>> queryParams;

        private HeaderlessAppRequest(AppRequest appRequest) {
            this.path = appRequest.getPath();
            this.method = appRequest.getMethod();
            this.body = appRequest.getBody();
            this.queryParams = appRequest.getQueryParams();
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;

            HeaderlessAppRequest that = (HeaderlessAppRequest) o;

            if(path != null ? !path.equals(that.path) : that.path != null) return false;
            if(method != null ? !method.equals(that.method) : that.method != null) return false;
            if(body != null ? !body.equals(that.body) : that.body != null) return false;
            return queryParams != null ? queryParams.equals(that.queryParams) : that.queryParams == null;

        }

        @Override
        public int hashCode() {
            int result = path != null ? path.hashCode() : 0;
            result = 31 * result + (method != null ? method.hashCode() : 0);
            result = 31 * result + (body != null ? body.hashCode() : 0);
            result = 31 * result + (queryParams != null ? queryParams.hashCode() : 0);
            return result;
        }
    }
}