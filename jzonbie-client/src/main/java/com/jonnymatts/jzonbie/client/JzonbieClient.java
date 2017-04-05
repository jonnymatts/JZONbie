package com.jonnymatts.jzonbie.client;

import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.response.DefaultAppResponse;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;

import java.io.File;
import java.util.List;

public interface JzonbieClient {

    ZombiePriming primeZombie(AppRequest request, AppResponse response);

    List<PrimedMapping> primeZombie(File file);

    ZombiePriming primeZombieForDefault(AppRequest request, DefaultAppResponse response);

    List<PrimedMapping> getCurrentPriming();

    List<ZombiePriming> getHistory();

    boolean verify(AppRequest appRequest);

    boolean verify(AppRequest appRequest, InvocationVerificationCriteria criteria);

    void reset();
}