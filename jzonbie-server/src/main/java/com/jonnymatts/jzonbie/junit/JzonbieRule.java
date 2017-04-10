package com.jonnymatts.jzonbie.junit;

import com.jonnymatts.jzonbie.Jzonbie;
import com.jonnymatts.jzonbie.JzonbieOptions;
import com.jonnymatts.jzonbie.model.AppRequest;
import com.jonnymatts.jzonbie.model.AppResponse;
import com.jonnymatts.jzonbie.model.PrimedMapping;
import com.jonnymatts.jzonbie.model.ZombiePriming;
import com.jonnymatts.jzonbie.response.DefaultAppResponse;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import com.jonnymatts.jzonbie.verification.VerificationException;
import org.junit.rules.ExternalResource;

import java.io.File;
import java.util.List;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;

public class JzonbieRule extends ExternalResource {

    private final JzonbieOptions options;
    private static Jzonbie jzonbie;

    private JzonbieRule(JzonbieOptions options) {
        this.options = options;
    }

    public static JzonbieRule jzonbie() {
        return new JzonbieRule(options());
    }

    public static JzonbieRule jzonbie(JzonbieOptions options) {
        return new JzonbieRule(options);
    }

    public static int port() {
        return jzonbie.getPort();
    }

    public static ZombiePriming prime(AppRequest appRequest, AppResponse appResponse) {
        return jzonbie.primeZombie(appRequest, appResponse);
    }

    public static ZombiePriming prime(AppRequest appRequest, DefaultAppResponse defaultAppResponse) {
        return jzonbie.primeZombieForDefault(appRequest, defaultAppResponse);
    }

    public static List<PrimedMapping> prime(File file) {
        return jzonbie.primeZombie(file);
    }

    public static void verify(AppRequest appRequest) throws VerificationException {
        jzonbie.verify(appRequest);
    }

    public static void verify(AppRequest appRequest, InvocationVerificationCriteria criteria) throws VerificationException {
        jzonbie.verify(appRequest, criteria);
    }

    public static List<PrimedMapping> currentPriming() {
        return jzonbie.getCurrentPriming();
    }

    public static void reset() {
        jzonbie.reset();
    }

    @Override
    protected void before() throws Throwable {
        jzonbie = new Jzonbie(options);
    }

    @Override
    protected void after() {
        jzonbie.stop();
    }
}