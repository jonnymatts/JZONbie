package com.jonnymatts.jzonbie.history;

import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;

public class Exchange {

    private AppRequest request;
    private AppResponse response;

    public Exchange() {}

    public Exchange(AppRequest request, AppResponse response) {
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

        Exchange that = (Exchange) o;

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
        return "Exchange{" +
                "request=" + request +
                ", response=" + response +
                '}';
    }
}
