package com.jonnymatts.jzonbie.jackson.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jonnymatts.jzonbie.responses.AppResponse;

import java.util.ArrayDeque;

public abstract class DefaultingQueueMixIn {

    @JsonProperty("primed")
    private ArrayDeque<AppResponse> deque;
}