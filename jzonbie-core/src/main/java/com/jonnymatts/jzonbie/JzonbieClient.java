package com.jonnymatts.jzonbie;

import com.jonnymatts.jzonbie.history.Exchange;
import com.jonnymatts.jzonbie.priming.PrimedMapping;
import com.jonnymatts.jzonbie.requests.AppRequest;
import com.jonnymatts.jzonbie.responses.AppResponse;
import com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse;
import com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria;
import com.jonnymatts.jzonbie.verification.VerificationException;

import java.io.File;
import java.security.KeyStore;
import java.util.List;

import static com.jonnymatts.jzonbie.verification.InvocationVerificationCriteria.equalTo;

/**
 * Interface defining common operations provided by Jzonbie
 */
public interface JzonbieClient {

    /**
     * Prime this Jzonbie to return response when an incoming request matches the input request.
     * <p>
     * This is a one-time priming. Once matched, the priming will be removed.
     * This priming will override any default priming.
     *
     * @param request  the request to match against
     * @param response the response this Jzonbie will return
     */
    void prime(AppRequest request, AppResponse response);

    /**
     * Prime this Jzonbie to return response when an incoming request matches the input request
     * and there are no responses primed.
     * <p>
     * This is a permanent priming. It can only be removed by resetting this Jzonbie.
     * However, it can be overridden by any standard priming.
     *
     * @param request  the request to match against
     * @param response the response this Jzonbie will return by default
     */
    void prime(AppRequest request, DefaultAppResponse response);

    /**
     * Prime this Jzonbie with the request/response priming contained within the input file.
     * <p>
     * This priming must be presented in the format required by Jzonbie (see docs).
     *
     * @param file the file containing the Jzonbie priming
     */
    void prime(File file);

    /**
     * Returns all priming currently configured for this Jzonbie.
     *
     * @return current priming
     */
    List<PrimedMapping> getCurrentPriming();

    /**
     * Returns the successful responses from this Jzonbie and the requests they matched against.
     *
     * @return request/response pairs for successful calls
     */
    List<Exchange> getHistory();

    /**
     * Returns the incoming requests that this Jzonbie failed to match against the stored priming.
     *
     * @return failed requests
     */
    List<AppRequest> getFailedRequests();

    /**
     * Verifies that the given {@link InvocationVerificationCriteria} is valid when matching against all
     * requests in this Jzonbies history.
     *
     * @param request  request to match against
     * @param criteria verification criteria
     * @throws VerificationException if the verification criteria is not valid for the matched requests
     */
    void verify(AppRequest request, InvocationVerificationCriteria criteria) throws VerificationException;

    /**
     * Verifies that the request can be matched against only one request in this Jzonbies history.
     * <p>
     * Convenience method for:
     * <p>
     * {@code
     * jzonbie.verify(request, equalTo(1));
     * }
     *
     * @param appRequest request to match against
     * @throws VerificationException if there is not only one requests that matches
     */
    default void verify(AppRequest appRequest) throws VerificationException {
        verify(appRequest, equalTo(1));
    }

    /**
     * Resets the state of this Jzonbie.
     * <p>
     * Resets:
     * <ul>
     * <li> Current history
     * <li> Failed requests
     * <li> Current priming - standard and default
     * </ul>
     */
    void reset();

    /**
     * Returns the generated Truststore (containing the public key) if Jzonbie is running with default HTTPS configuration.
     * <p>
     * Throws exception if Jzonbie is not running with default HTTPS configuration.
     *
     * @return truststore containing generated public key
     */
    KeyStore getTruststore();
}