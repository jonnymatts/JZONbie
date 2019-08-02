package com.jonnymatts.jzonbie.history;

import com.jonnymatts.jzonbie.requests.AppRequest;


public class CallHistory extends FixedCapacityCache<Exchange> {

    public CallHistory(int capacity) {
        super(capacity);
    }

    public int count(AppRequest appRequest) {
        return (int)values.stream().filter(priming -> appRequest.matches(priming.getRequest())).count();
    }
}