package com.jonnymatts.jzonbie.responses;

import com.jonnymatts.jzonbie.Response;
import com.jonnymatts.jzonbie.body.ArrayBodyContent;
import com.jonnymatts.jzonbie.body.BodyContent;
import com.jonnymatts.jzonbie.body.LiteralBodyContent;
import com.jonnymatts.jzonbie.body.ObjectBodyContent;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

import static com.jonnymatts.jzonbie.body.ArrayBodyContent.arrayBody;
import static com.jonnymatts.jzonbie.body.LiteralBodyContent.literalBody;
import static com.jonnymatts.jzonbie.body.ObjectBodyContent.objectBody;
import static com.jonnymatts.jzonbie.util.Copier.copyBodyContent;
import static com.jonnymatts.jzonbie.util.Copier.copyMap;
import static java.util.Optional.ofNullable;

/**
 * Defines the response Jzonbie will return once a request is matched.
 * <p>
 * A response must be configured with a status code. Static factory methods are provided
 * for the most common response status codes. The response headers, body, and a delay before
 * responding can also be configured.
 * <p>
 * When creating responses, it is recommended to use the builder "withX" methods.
 * <p>
 * {@code
 * final AppResponse response = ok()
 *                 .contentType("application/json")
 *                 .withHeader("Version", "3")
 *                 .withBody(singletonMap("data", "value"));
 * }
 * <p>
 * A response may also be "templated", which allows it to refer to the matched incoming
 * request when defining its headers and body. For more information, see the Jzonbie docs.
 */
public class AppResponse implements Response {

    private int statusCode;
    private Map<String, String> headers;
    private Duration delay;
    private BodyContent<?> body;
    private boolean templated;

    public AppResponse() {
    }

    /**
     * Creates a new {@code AppResponse} with the given status code.
     * <p>
     * It is recommended to use the provided static factory methods.
     * <p>
     * eg. {@link #ok()}, {@link #badRequest()}, {@link #internalServerError()}, etc.
     *
     * @param statusCode status code of the response
     */
    public AppResponse(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Creates a new {@code AppResponse} that copies all fields of the input {@code AppResponse}.
     *
     * @param response response to copy
     */
    @SuppressWarnings("CopyConstructorMissesField")
    public AppResponse(AppResponse response) {
        setStatusCode(response.getStatusCode());
        setHeaders(copyMap(response.getHeaders()));
        setBody(copyBodyContent(response.getBody()));
        response.getDelay().ifPresent(this::setDelay);
        setTemplated(response.isTemplated());
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public BodyContent<?> getBody() {
        return body;
    }

    public void setBody(BodyContent<?> body) {
        this.body = body;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setDelay(Duration delay) {
        this.delay = delay;
    }

    @Override
    public Optional<Duration> getDelay() {
        return ofNullable(delay);
    }

    @Override
    public boolean isTemplated() {
        return templated;
    }

    public void setTemplated(boolean templated) {
        this.templated = templated;
    }

    /**
     * Enables response templating on this response.
     *
     * @return this response with templating enabled
     */
    public AppResponse templated() {
        this.setTemplated(true);
        return this;
    }

    /**
     * Adds the header to this response.
     *
     * @param name header name
     * @param value header value
     * @return this response with the added header
     */
    public AppResponse withHeader(String name, String value) {
        if(this.getHeaders() == null)
            this.setHeaders(new HashMap<>());
        this.getHeaders().put(name, value);
        return this;
    }

    /**
     * Configures this response with the given {@link BodyContent} body.
     *
     * @param body body content
     * @return this response with a body
     */
    public AppResponse withBody(BodyContent<?> body) {
        this.setBody(body);
        return this;
    }

    /**
     * Configures this response with an {@link ObjectBodyContent} body.
     *
     * @param body object body
     * @return this response with a body
     */
    public AppResponse withBody(Map<String, ?> body) {
        this.setBody(objectBody(body));
        return this;
    }

    /**
     * Configures this response with a {@link LiteralBodyContent} body.
     *
     * @param body string body
     * @return this response with a body
     */
    public AppResponse withBody(String body) {
        this.setBody(literalBody(body));
        return this;
    }

    /**
     * Configures this response with an {@link ArrayBodyContent} body.
     *
     * @param body list body
     * @return this response with a body
     */
    public AppResponse withBody(List<?> body) {
        this.setBody(arrayBody(body));
        return this;
    }

    /**
     * Configures this response with a {@link BigDecimal} {@link LiteralBodyContent} body.
     *
     * @param body number body
     * @return this response with a body
     */
    public AppResponse withBody(Number body) {
        this.setBody(literalBody(new BigDecimal(body.doubleValue())));
        return this;
    }

    /**
     * Sets the duration Jzonbie should delay for before responding with this response.
     *
     * @param delay delay duration
     * @return this response with a delay
     */
    public AppResponse withDelay(Duration delay) {
        this.setDelay(delay);
        return this;
    }

    /**
     * Sets the "Content-Type" header of this response.
     *
     * @param contentType content type header value
     * @return this response with a content type header
     */
    public AppResponse contentType(String contentType) {
        return withHeader("Content-Type", contentType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppResponse that = (AppResponse) o;
        return statusCode == that.statusCode &&
                templated == that.templated &&
                Objects.equals(headers, that.headers) &&
                Objects.equals(delay, that.delay) &&
                Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode, headers, delay, body, templated);
    }

    @Override
    public String toString() {
        return "AppResponse{" +
                "statusCode=" + statusCode +
                ", headers=" + headers +
                ", delay=" + delay +
                ", body=" + body +
                ", templated=" + templated +
                '}';
    }

    /**
     * Returns a response with the given status code.
     *
     * @param statusCode status code of the response
     * @return response with status code
     */
    public static AppResponse response(int statusCode) {
        return new AppResponse(statusCode);
    }

    /**
     * Returns a response with the 200(OK) status code.
     *
     * @return 200(OK) response
     */
    public static AppResponse ok() {
        return new AppResponse(200);
    }

    /**
     * Returns a response with the 201(Created) status code.
     *
     * @return 201(Created) response
     */
    public static AppResponse created() {
        return new AppResponse(201);
    }

    /**
     * Returns a response with the 202(Accepted) status code.
     *
     * @return 202(Accepted) response
     */
    public static AppResponse accepted() {
        return new AppResponse(202);
    }

    /**
     * Returns a response with the 204(No Content) status code.
     *
     * @return 204(No Content) response
     */
    public static AppResponse noContent() {
        return new AppResponse(204);
    }

    /**
     * Returns a response with the 400(Bad Request) status code.
     *
     * @return 400(Bad Request) response
     */
    public static AppResponse badRequest() {
        return new AppResponse(400);
    }

    /**
     * Returns a response with the 401(Unauthorized) status code.
     *
     * @return 401(Unauthorized) response
     */
    public static AppResponse unauthorized() {
        return new AppResponse(401);
    }

    /**
     * Returns a response with the 403(Forbidden) status code.
     *
     * @return 403(Forbidden) response
     */
    public static AppResponse forbidden() {
        return new AppResponse(403);
    }

    /**
     * Returns a response with the 404(Not Found) status code.
     *
     * @return 404(Not Found) response
     */
    public static AppResponse notFound() {
        return new AppResponse(404);
    }

    /**
     * Returns a response with the 405(Method Not Allowed) status code.
     *
     * @return 405(Method Not Allowed) response
     */
    public static AppResponse methodNotAllowed() {
        return new AppResponse(405);
    }

    /**
     * Returns a response with the 409(Conflict) status code.
     *
     * @return 409(Conflict) response
     */
    public static AppResponse conflict() {
        return new AppResponse(409);
    }

    /**
     * Returns a response with the 500(Internal Server Error) status code.
     *
     * @return 500(Internal Server Error) response
     */
    public static AppResponse internalServerError() {
        return new AppResponse(500);
    }

    /**
     * Returns a response with the 503(Service Unavailable) status code.
     *
     * @return 503(Service Unavailable) response
     */
    public static AppResponse serviceUnavailable() {
        return new AppResponse(503);
    }

    /**
     * Returns a response with the 504(Gateway Timeout) status code.
     *
     * @return 504(Gateway Timeout) response
     */
    public static AppResponse gatewayTimeout() {
        return new AppResponse(504);
    }
}