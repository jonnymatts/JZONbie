package com.jonnymatts.jzonbie.client;

import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.ZombiePriming;

import java.util.List;

public interface JzonbieClient {

    ZombiePriming primeZombie(AppRequest request, AppResponse response);

    List<PrimedMapping> getCurrentPriming();

    List<ZombiePriming> getHistory();

    void reset();
}
