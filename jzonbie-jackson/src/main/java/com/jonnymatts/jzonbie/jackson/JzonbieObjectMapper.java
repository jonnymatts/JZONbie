package com.jonnymatts.jzonbie.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jonnymatts.jzonbie.body.*;
import com.jonnymatts.jzonbie.jackson.body.*;
import com.jonnymatts.jzonbie.jackson.responses.DefaultAppResponseMixIn;
import com.jonnymatts.jzonbie.jackson.responses.DefaultingQueueMixIn;
import com.jonnymatts.jzonbie.jackson.responses.StaticDefaultAppResponseMixIn;
import com.jonnymatts.jzonbie.jackson.verification.InvocationVerificationCriteriaMixIn;
import com.jonnymatts.jzonbie.responses.DefaultAppResponse;
import com.jonnymatts.jzonbie.responses.DefaultAppResponse.StaticDefaultAppResponse;
import com.jonnymatts.jzonbie.responses.DefaultingQueue;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

public class JzonbieObjectMapper extends ObjectMapper {

    public JzonbieObjectMapper() {
        super();
        registerModule(new Jdk8Module());
        registerModule(new JavaTimeModule());
        enable(INDENT_OUTPUT);
        setSerializationInclusion(NON_NULL);

        addMixIn(BodyContent.class, BodyContentMixIn.class);
        addMixIn(ArrayBodyContent.class, ArrayBodyContentMixIn.class);
        addMixIn(LiteralBodyContent.class, LiteralBodyContentMixIn.class);
        addMixIn(ObjectBodyContent.class, ObjectBodyContentMixIn.class);
        addMixIn(StringBodyContent.class, StringBodyContentMixIn.class);
        addMixIn(InvocationVerificationCriteria.class, InvocationVerificationCriteriaMixIn.class);
        addMixIn(DefaultAppResponse.class, DefaultAppResponseMixIn.class);
        addMixIn(StaticDefaultAppResponse.class, StaticDefaultAppResponseMixIn.class);
        addMixIn(DefaultingQueue.class, DefaultingQueueMixIn.class);
    }
}
