package com.jonnymatts.jzonbie.requests;

import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.PrimingContext;
import com.jonnymatts.jzonbie.response.DefaultingQueue;

import java.util.List;

public class PrimedMappingUploader {

    private final PrimingContext primingContext;

    public PrimedMappingUploader(PrimingContext primingContext) {
        this.primingContext = primingContext;
    }

    public void upload(List<PrimedMapping> primedMappings) {
        primedMappings.forEach(primedMapping -> {
            final DefaultingQueue defaultingQueue = primedMapping.getAppResponses();
            defaultingQueue.getEntries().forEach(appResponse -> primingContext.add(primedMapping.getAppRequest(), appResponse));
            defaultingQueue.getDefault().map(defaultResponse -> primingContext.addDefault(primedMapping.getAppRequest(), defaultResponse));
        });
    }
}