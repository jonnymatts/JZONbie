package com.jonnymatts.jzonbie.responses.defaults;

import com.jonnymatts.jzonbie.responses.AppResponse;

import static com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponseType.STATIC;

/**
 * A default response that will always respond with the same response.
 */
public class StaticDefaultAppResponse extends DefaultAppResponse {
    private AppResponse response;

    /**
     * Returns a {@code StaticDefaultAppResponse} with the response.
     * <p>
     * Use {@link DefaultAppResponse#staticDefault factory method} instead.
     *
     * @param response static response
     */
    public StaticDefaultAppResponse(AppResponse response) {
        this.response = response;
    }

    @Override
    public AppResponse getResponse() {
        return response;
    }

    @Override
    public DefaultAppResponseType getType() {
        return STATIC;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StaticDefaultAppResponse that = (StaticDefaultAppResponse) o;

        return response != null ? response.equals(that.response) : that.response == null;
    }

    @Override
    public int hashCode() {
        return response != null ? response.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "StaticDefaultAppResponse{" +
                "response=" + response +
                '}';
    }
}