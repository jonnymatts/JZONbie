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

public interface JzonbieClient {

    ZombiePriming prime(AppRequest request, AppResponse response);

    List<PrimedMapping> prime(File file);

    ZombiePriming prime(AppRequest request, DefaultAppResponse response);

    List<PrimedMapping> getCurrentPriming();

    List<ZombiePriming> getHistory();

    void reset();

    void verify(AppRequest appRequest, InvocationVerificationCriteria criteria) throws VerificationException;

    default void verify(AppRequest appRequest) throws VerificationException {
        verify(appRequest, equalTo(1));
    }
}