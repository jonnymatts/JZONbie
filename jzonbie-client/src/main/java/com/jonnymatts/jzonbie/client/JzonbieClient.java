package com.jonnymatts.jzonbie.client;

import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.response.DefaultAppResponse;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import com.jonnymatts.jzonbie.verification.VerificationException;

import java.io.File;
import java.util.List;

import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.equalTo;

public abstract class JzonbieClient {

    public abstract ZombiePriming primeZombie(AppRequest request, AppResponse response);

    public abstract List<PrimedMapping> primeZombie(File file);

    public abstract ZombiePriming primeZombieForDefault(AppRequest request, DefaultAppResponse response);

    public abstract List<PrimedMapping> getCurrentPriming();

    public abstract List<ZombiePriming> getHistory();

    public abstract void reset();

    public abstract void verify(AppRequest appRequest, InvocationVerificationCriteria criteria) throws VerificationException;

    public void verify(AppRequest appRequest) throws VerificationException {
        verify(appRequest, equalTo(1));
    }
}