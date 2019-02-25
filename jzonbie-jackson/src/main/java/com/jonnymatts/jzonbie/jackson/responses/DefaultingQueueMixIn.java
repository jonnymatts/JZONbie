package com.jonnymatts.jzonbie.jackson.responses;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = DefaultingQueueSerializer.class)
@JsonDeserialize(using = DefaultingQueueDeserializer.class)
public abstract class DefaultingQueueMixIn {
}