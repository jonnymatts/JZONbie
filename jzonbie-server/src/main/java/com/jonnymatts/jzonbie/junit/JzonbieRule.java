package com.jonnymatts.jzonbie.junit;

import com.jonnymatts.jzonbie.Jzonbie;
import com.jonnymatts.jzonbie.JzonbieOptions;
import com.jonnymatts.jzonbie.client.JzonbieClient;
import com.jonnymatts.jzonbie.model.*;
import com.jonnymatts.jzonbie.response.DefaultAppResponse;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import com.jonnymatts.jzonbie.verification.VerificationException;
import org.junit.rules.ExternalResource;

import java.io.File;
import java.util.List;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;

public class JzonbieRule extends ExternalResource implements JzonbieClient {

    private final JzonbieOptions options;
    private Jzonbie jzonbie;

    private JzonbieRule(JzonbieOptions options) {
        this.options = options;
    }

    public static JzonbieRule jzonbie() {
        return new JzonbieRule(options());
    }

    public static JzonbieRule jzonbie(JzonbieOptions options) {
        return new JzonbieRule(options);
    }

    public int getPort() {
        return jzonbie.getPort();
    }

    public ZombiePriming prime(AppRequest appRequest, AppResponse appResponse) {
        return jzonbie.prime(appRequest, appResponse);
    }

    @Override
    public ZombiePriming prime(AppRequest appRequest, TemplatedAppResponse templatedAppResponse) {
        return jzonbie.prime(appRequest, templatedAppResponse);
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
    protected void before() throws Throwable {
        jzonbie = new Jzonbie(options);
    }

    @Override
    protected void after() {
        jzonbie.stop();
    }
}