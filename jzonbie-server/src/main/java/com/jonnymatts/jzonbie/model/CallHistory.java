package com.jonnymatts.jzonbie.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;

import java.util.ArrayList;
import java.util.List;

import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.equalTo;


public class CallHistory {

    private final List<ZombiePriming> history;

    public CallHistory() {
        this.history = new ArrayList<>();
    }

    public CallHistory(List<ZombiePriming> history) {
        this.history = history;
    }

    @JsonValue
    public List<ZombiePriming> getEntries() {
        return history;
    }

    public void add(ZombiePriming priming) {
        history.add(priming);
    }

    public void clear() {
        history.clear();
    }

    public boolean verify(AppRequest appRequest) {
        return verify(appRequest, equalTo(1));
    }

    public boolean verify(AppRequest appRequest, InvocationVerificationCriteria criteria) {
        final int callCount = (int)history.stream().filter(priming -> appRequest.matches(priming.getAppRequest())).count();
        return criteria.accept(callCount);
    }
}