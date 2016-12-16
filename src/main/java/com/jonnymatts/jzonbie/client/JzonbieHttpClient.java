package com.jonnymatts.jzonbie.client;

import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.model.ZombieRequest;
import com.jonnymatts.jzonbie.model.ZombieResponse;

import java.util.List;

public interface JzonbieHttpClient {

    ZombiePriming primeZombie(ZombieRequest request, ZombieResponse response);

    List<PrimedMapping> getCurrentPriming();

    List<ZombiePriming> getHistory();

    void reset();
}
