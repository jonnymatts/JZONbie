package com.jonnymatts.jzonbie.client;

import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.response.DefaultResponse;

import java.util.List;

public interface JzonbieClient {

    ZombiePriming primeZombie(AppRequest request, AppResponse response);

    ZombiePriming primeZombieForDefault(AppRequest request, DefaultResponse<AppResponse> response);

    List<PrimedMapping> getCurrentPriming();

    List<ZombiePriming> getHistory();

    void reset();
}