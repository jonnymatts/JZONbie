package com.jonnymatts.jzonbie.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.List;


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

    public int count(AppRequest appRequest) {
        return (int)history.stream().filter(priming -> appRequest.matches(priming.getAppRequest())).count();
    }
}