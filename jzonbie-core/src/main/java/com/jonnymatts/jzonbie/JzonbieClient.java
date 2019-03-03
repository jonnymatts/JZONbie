package com.jonnymatts.jzonbie;

import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import com.jonnymatts.jzonbie.verification.VerificationException;

import java.io.File;
import java.util.List;

import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.equalTo;

public interface JzonbieClient {

    ZombiePriming prime(AppRequest request, AppResponse response);

    ZombiePriming prime(AppRequest request, DefaultAppResponse response);

    List<PrimedMapping> prime(File file);

    List<PrimedMapping> getCurrentPriming();

    List<ZombiePriming> getHistory();

    List<AppRequest> getFailedRequests();

    void verify(AppRequest appRequest, InvocationVerificationCriteria criteria) throws VerificationException;

    default void verify(AppRequest appRequest) throws VerificationException {
        verify(appRequest, equalTo(1));
    }

    void reset();
}