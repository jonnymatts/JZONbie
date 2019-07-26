package com.jonnymatts.jzonbie.junit;

import com.jonnymatts.jzonbie.Jzonbie;
import com.jonnymatts.jzonbie.JzonbieClient;
import com.jonnymatts.jzonbie.JzonbieOptions;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.priming.ZombiePriming;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import com.jonnymatts.jzonbie.verification.VerificationException;
import org.junit.rules.ExternalResource;

import java.io.File;
import java.security.KeyStore;
import java.util.List;
import java.util.function.Supplier;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;

public class JzonbieRule<T extends Jzonbie> extends ExternalResource implements JzonbieClient {

    private Supplier<T> jzonbieCreator;
    private T jzonbie;

    private JzonbieRule(Supplier<T> jzonbieCreator) {
        this.jzonbieCreator = jzonbieCreator;
    }

    public static JzonbieRule<Jzonbie> jzonbie() {
        return jzonbie(options());
    }

    public static JzonbieRule<Jzonbie> jzonbie(JzonbieOptions options) {
        return new JzonbieRule<>(() -> new Jzonbie(options));
    }

    public static <T extends Jzonbie> JzonbieRule<T> jzonbie(Supplier<T> jzonbie) {
        return new JzonbieRule<>(jzonbie);
    }

    public int getHttpPort() {
        return jzonbie.getHttpPort();
    }

    public int getHttpsPort() {
        return jzonbie.getHttpsPort();
    }

    public ZombiePriming prime(AppRequest appRequest, AppResponse appResponse) {
        return jzonbie.prime(appRequest, appResponse);
    }

    public ZombiePriming prime(AppRequest appRequest, DefaultAppResponse defaultAppResponse) {
        return jzonbie.prime(appRequest, defaultAppResponse);
    }

    public List<PrimedMapping> prime(File file) {
        return jzonbie.prime(file);
    }

    public void verify(AppRequest appRequest) throws VerificationException {
        jzonbie.verify(appRequest);
    }

    public void verify(AppRequest appRequest, InvocationVerificationCriteria criteria) throws VerificationException {
        jzonbie.verify(appRequest, criteria);
    }

    public List<PrimedMapping> getCurrentPriming() {
        return jzonbie.getCurrentPriming();
    }

    public List<ZombiePriming> getHistory() {
        return jzonbie.getHistory();
    }

    public List<AppRequest> getFailedRequests() {
        return jzonbie.getFailedRequests();
    }

    public void reset() {
        jzonbie.reset();
    }

    @Override
    public KeyStore getTruststore() {
        return jzonbie.getTruststore();
    }

    public T getJzonbie() {
        return jzonbie;
    }

    @Override
    protected void before() throws Throwable {
        if(jzonbie == null) {
            jzonbie = jzonbieCreator.get();
        }
    }

    @Override
    protected void after() {
        jzonbie.stop();
    }
}