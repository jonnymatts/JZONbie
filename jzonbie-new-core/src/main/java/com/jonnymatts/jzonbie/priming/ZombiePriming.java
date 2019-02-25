package com.jonnymatts.jzonbie.priming;

import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;

public class ZombiePriming {

    private AppRequest request;
    private AppResponse response;

    public ZombiePriming() {}

    public ZombiePriming(AppRequest request, AppResponse response) {
        this.request = request;
        this.response = response;
    }

    public AppRequest getRequest() {
        return request;
    }

    public void setRequest(AppRequest request) {
        this.request = request;
    }

    public AppResponse getResponse() {
        return response;
    }

    public void setResponse(AppResponse response) {
        this.response = response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZombiePriming that = (ZombiePriming) o;

        if (request != null ? !request.equals(that.request) : that.request != null)
            return false;
        return response != null ? response.equals(that.response) : that.response == null;
    }

    @Override
    public int hashCode() {
        int result = request != null ? request.hashCode() : 0;
        result = 31 * result + (response != null ? response.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ZombiePriming{" +
                "request=" + request +
                ", response=" + response +
                '}';
    }
}
