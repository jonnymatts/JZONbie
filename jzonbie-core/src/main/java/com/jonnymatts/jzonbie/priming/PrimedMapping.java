package com.jonnymatts.jzonbie.priming;

import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.defaults.DefaultingQueue;

public class PrimedMapping {

    public PrimedMapping() {}

    public PrimedMapping(AppRequest request, DefaultingQueue responses) {
        this.request = request;
        this.responses = responses;
    }

    private AppRequest request;
    private DefaultingQueue responses;

    public AppRequest getRequest() {
        return request;
    }

    public void setRequest(AppRequest request) {
        this.request = request;
    }

    public DefaultingQueue getResponses() {
        return responses;
    }

    public void setResponses(DefaultingQueue responses) {
        this.responses = responses;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        PrimedMapping that = (PrimedMapping) o;

        if(request != null ? !request.equals(that.request) : that.request != null) return false;
        return responses != null ? responses.equals(that.responses) : that.responses == null;

    }

    @Override
    public int hashCode() {
        int result = request != null ? request.hashCode() : 0;
        result = 31 * result + (responses != null ? responses.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PrimedMapping{" +
                "request=" + request +
                ", responses=" + responses +
                '}';
    }
}
