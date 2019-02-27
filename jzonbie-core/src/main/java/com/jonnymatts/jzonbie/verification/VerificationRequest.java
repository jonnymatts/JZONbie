package com.jonnymatts.jzonbie.verification;

import com.jonnymatts.jzonbie.requests.AppRequest;

public class VerificationRequest {
    private AppRequest appRequest;
    private InvocationVerificationCriteria criteria;

    public VerificationRequest() {
    }

    public VerificationRequest(AppRequest appRequest, InvocationVerificationCriteria criteria) {
        this.appRequest = appRequest;
        this.criteria = criteria;
    }

    public AppRequest getAppRequest() {
        return appRequest;
    }

    public InvocationVerificationCriteria getCriteria() {
        return criteria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VerificationRequest that = (VerificationRequest) o;

        if (appRequest != null ? !appRequest.equals(that.appRequest) : that.appRequest != null) return false;
        return criteria != null ? criteria.equals(that.criteria) : that.criteria == null;
    }

    @Override
    public int hashCode() {
        int result = appRequest != null ? appRequest.hashCode() : 0;
        result = 31 * result + (criteria != null ? criteria.hashCode() : 0);
        return result;
    }
}
