package com.jonnymatts.jzonbie.junit;

import com.jonnymatts.jzonbie.Jzonbie;
import com.jonnymatts.jzonbie.JzonbieClient;
import com.jonnymatts.jzonbie.JzonbieOptions;
import com.jonnymatts.jzonbie.history.Exchange;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import com.jonnymatts.jzonbie.verification.VerificationException;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExternalResource;

import java.io.File;
import java.security.KeyStore;
import java.util.List;
import java.util.function.Supplier;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;

/**
 * JUnit {@link Rule} that provides a {@link Jzonbie} in a test suite.
 * <p>
 * Every test method will receive a new instance of Jzonbie.
 * <pre>
 * &#64;Rule
 * public JzonbieRule jzonbieRule = JzonbieRule.jzonbie();
 * </pre>
 * <p>
 * The {@link ClassRule} annotation can be used to receive the same Jzonbie instance
 * for every method. When using this annotation, the Jzonbie must be reset after every
 * test.
 * <p>
 * <pre>
 * &#64;ClassRule
 * public static JzonbieRule jzonbieRule = JzonbieRule.jzonbie();
 *
 * &#64;Before
 * public void setUp() throws Exception {
 *     jzonbieRule.reset();
 * }
 * </pre>
 *
 * @param <T> class of Jzonbie instance
 */
public class JzonbieRule<T extends Jzonbie> extends ExternalResource implements JzonbieClient {

    private Supplier<T> jzonbieCreator;
    private T jzonbie;

    private JzonbieRule(Supplier<T> jzonbieCreator) {
        this.jzonbieCreator = jzonbieCreator;
    }

    /**
     * Returns a {@code JzonbieRule} with a default Jzonbie.
     * <p>
     * <pre>
     * &#64;Rule
     * public JzonbieRule jzonbieRule = JzonbieRule.jzonbie();
     * </pre>
     * @return default Jzonbie rule
     */
    public static JzonbieRule<Jzonbie> jzonbie() {
        return jzonbie(options());
    }

    /**
     * Returns a {@code JzonbieRule} with a customized Jzonbie.
     * <p>
     * <pre>
     * &#64;Rule
     * public JzonbieRule jzonbieRule = JzonbieRule.jzonbie(options().withHttpPort(8080));
     * </pre>
     * @return default Jzonbie rule
     */
    public static JzonbieRule<Jzonbie> jzonbie(JzonbieOptions options) {
        return new JzonbieRule<>(() -> new Jzonbie(options));
    }

    /**
     * Returns a {@code JzonbieRule} with a Jzonbie of the supplied class.
     * <p>
     * <pre>
     * &#64;Rule
     * public JzonbieRule jzonbieRule = JzonbieRule.jzonbie(CustomJzonbie::new);
     * </pre>
     *
     * @param jzonbie supplier of custom jzonbie
     * @param <T> class extends Jzonbie
     * @return customized Jzonbie rule
     */
    public static <T extends Jzonbie> JzonbieRule<T> jzonbie(Supplier<T> jzonbie) {
        return new JzonbieRule<>(jzonbie);
    }

    /**
     * Returns the port the HTTP server is listening on.
     *
     * @return http port
     */
    public int getHttpPort() {
        return jzonbie.getHttpPort();
    }

    /**
     * Returns the port the HTTPS server is listening on.
     *
     * @exception IllegalStateException if Jzonbie is not running with HTTPS enabled
     * @return https port
     */
    public int getHttpsPort() {
        return jzonbie.getHttpsPort();
    }

    public void prime(AppRequest appRequest, AppResponse appResponse) {
        jzonbie.prime(appRequest, appResponse);
    }

    public void prime(AppRequest appRequest, DefaultAppResponse defaultAppResponse) {
        jzonbie.prime(appRequest, defaultAppResponse);
    }

    public void prime(File file) {
        jzonbie.prime(file);
    }

    public void verify(AppRequest appRequest) throws VerificationException {
        jzonbie.verify(appRequest);
    }

    public void verify(AppRequest request, InvocationVerificationCriteria criteria) throws VerificationException {
        jzonbie.verify(request, criteria);
    }

    public List<PrimedMapping> getCurrentPriming() {
        return jzonbie.getCurrentPriming();
    }

    public List<Exchange> getHistory() {
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

    /**
     * Returns the underlying Jzonbie.
     * <p>
     * <pre>
     * &#64;Rule
     * public JzonbieRule&#60;CustomJzonbie&#62; jzonbieRule = JzonbieRule.jzonbie(CustomJzonbie::new);
     *
     * ...
     *
     * final CustomJzonbie customJzonbie = jzonbieRule.getJzonbie();
     * </pre>
     *
     * @return jzonbie instance
     */
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