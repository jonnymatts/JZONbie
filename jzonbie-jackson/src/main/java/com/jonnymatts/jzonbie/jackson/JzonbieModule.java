package com.jonnymatts.jzonbie.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jonnymatts.jzonbie.body.*;
import com.jonnymatts.jzonbie.jackson.body.*;
import com.jonnymatts.jzonbie.jackson.responses.DefaultAppResponseMixIn;
import com.jonnymatts.jzonbie.jackson.responses.DefaultingQueueMixIn;
import com.jonnymatts.jzonbie.jackson.responses.DynamicDefaultAppResponseMixIn;
import com.jonnymatts.jzonbie.jackson.responses.StaticDefaultAppResponseMixIn;
import com.jonnymatts.jzonbie.jackson.verification.InvocationVerificationCriteriaMixIn;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultingQueue;
import com.jonnymatts.jzonbie.responses.defaults.DynamicDefaultAppResponse;
import com.jonnymatts.jzonbie.responses.defaults.StaticDefaultAppResponse;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;

public class JzonbieModule extends SimpleModule {

    public JzonbieModule() {
        super("jzonbie");

        setMixInAnnotation(BodyContent.class, BodyContentMixIn.class);
        setMixInAnnotation(ArrayBodyContent.class, ArrayBodyContentMixIn.class);
        setMixInAnnotation(LiteralBodyContent.class, LiteralBodyContentMixIn.class);
        setMixInAnnotation(ObjectBodyContent.class, ObjectBodyContentMixIn.class);
        setMixInAnnotation(StringBodyContent.class, StringBodyContentMixIn.class);
        setMixInAnnotation(InvocationVerificationCriteria.class, InvocationVerificationCriteriaMixIn.class);
        setMixInAnnotation(DefaultAppResponse.class, DefaultAppResponseMixIn.class);
        setMixInAnnotation(StaticDefaultAppResponse.class, StaticDefaultAppResponseMixIn.class);
        setMixInAnnotation(DynamicDefaultAppResponse.class, DynamicDefaultAppResponseMixIn.class);
        setMixInAnnotation(DefaultingQueue.class, DefaultingQueueMixIn.class);
    }
}