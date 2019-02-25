package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.priming.PrimingContext;
import com.jonnymatts.jzonbie.responses.DefaultingQueue;

import java.util.List;

public class PrimedMappingUploader {

    private final PrimingContext primingContext;

    public PrimedMappingUploader(PrimingContext primingContext) {
        this.primingContext = primingContext;
    }

    public void upload(List<PrimedMapping> primedMappings) {
        primedMappings.forEach(primedMapping -> {
            final DefaultingQueue defaultingQueue = primedMapping.getResponses();
            defaultingQueue.getPrimed().forEach(appResponse -> primingContext.add(primedMapping.getRequest(), appResponse));
            defaultingQueue.getDefault().map(defaultResponse -> primingContext.addDefault(primedMapping.getRequest(), defaultResponse));
        });
    }
}