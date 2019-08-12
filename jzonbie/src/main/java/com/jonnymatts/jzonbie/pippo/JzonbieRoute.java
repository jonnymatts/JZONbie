package com.jonnymatts.jzonbie.pippo;

import com.jonnymatts.jzonbie.Jzonbie;
import com.jonnymatts.jzonbie.JzonbieClient;
import com.jonnymatts.jzonbie.jackson.Deserializer;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.Routing;

import java.util.function.Consumer;

import static com.jonnymatts.jzonbie.pippo.JzonbieRoute.Method.*;

/**
 * Class that defines an endpoint that will be added to a custom Jzonbie.
 * <p>
 * A {@code JzonbieRoute} allows a custom endpoint to be added for a request
 * with a given HTTP method and path.
 * When handling the request, the {@code JzonbieRoute} provides a {@link JzonbieRouteContext}.
 * The context contains the current {@link RouteContext}, {@link Deserializer} and a {@link JzonbieClient}
 * for the Jzonbie instance.
 *
 * <pre>
 * {@code
 * options().withRoutes(
 *      JzonbieRoute.get("/ready", ctx -> ctx.getRouteContext().getResponse().ok())
 * )
 * }
 * </pre>
 */
public class JzonbieRoute implements Consumer<Routing> {

    private final Method method;
    private final String path;
    private final Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer;
    private JzonbieClient jzonbieClient;
    private Deserializer deserializer;

    private JzonbieRoute(Method method, String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        this.method = method;
        this.path = path;
        this.jzonbieRouteContextConsumer = jzonbieRouteContextConsumer;
    }

    @Override
    public void accept(Routing routing) {
        routing.addRoute(
                new Route(
                        method.name(),
                        path,
                        routeContext -> jzonbieRouteContextConsumer.accept(new JzonbieRouteContext(jzonbieClient, deserializer, routeContext))
                )
        );
    }

    public void setJzonbieClient(JzonbieClient jzonbieClient) {
        this.jzonbieClient = jzonbieClient;
    }

    public void setDeserializer(Deserializer deserializer) {
        this.deserializer = deserializer;
    }

    /**
     * Returns a JzonbieRoute for the given HTTP method and path.
     *
     * @param method HTTP method
     * @param path path
     * @param jzonbieRouteContextConsumer route context consumer
     * @return route for HTTP method and path
     */
    public static JzonbieRoute route(Method method, String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(method, path, jzonbieRouteContextConsumer);
    }

    /**
     * Returns a JzonbieRoute for the given path and any HTTP method.
     *
     * @param path path
     * @param jzonbieRouteContextConsumer route context consumer
     * @return route for path and any HTTP method
     */
    public static JzonbieRoute any(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(ANY, path, jzonbieRouteContextConsumer);
    }

    /**
     * Returns a HTTP CONNECT JzonbieRoute for the given path.
     *
     * @param path path
     * @param jzonbieRouteContextConsumer route context consumer
     * @return HTTP CONNECT route for path
     */
    public static JzonbieRoute connect(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(CONNECT, path, jzonbieRouteContextConsumer);
    }

    /**
     * Returns a HTTP DELETE JzonbieRoute for the given path.
     *
     * @param path path
     * @param jzonbieRouteContextConsumer route context consumer
     * @return HTTP DELETE route for path
     */
    public static JzonbieRoute delete(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(DELETE, path, jzonbieRouteContextConsumer);
    }

    /**
     * Returns a HTTP GET JzonbieRoute for the given path.
     *
     * @param path path
     * @param jzonbieRouteContextConsumer route context consumer
     * @return HTTP GET route for path
     */
    public static JzonbieRoute get(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(GET, path, jzonbieRouteContextConsumer);
    }

    /**
     * Returns a HTTP HEAD JzonbieRoute for the given path.
     *
     * @param path path
     * @param jzonbieRouteContextConsumer route context consumer
     * @return HTTP HEAD route for path
     */
    public static JzonbieRoute head(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(HEAD, path, jzonbieRouteContextConsumer);
    }

    /**
     * Returns a HTTP OPTIONS JzonbieRoute for the given path.
     *
     * @param path path
     * @param jzonbieRouteContextConsumer route context consumer
     * @return HTTP OPTIONS route for path
     */
    public static JzonbieRoute options(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(OPTIONS, path, jzonbieRouteContextConsumer);
    }

    /**
     * Returns a HTTP PATCH JzonbieRoute for the given path.
     *
     * @param path path
     * @param jzonbieRouteContextConsumer route context consumer
     * @return HTTP PATCH route for path
     */
    public static JzonbieRoute patch(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(PATCH, path, jzonbieRouteContextConsumer);
    }

    /**
     * Returns a HTTP POST JzonbieRoute for the given path.
     *
     * @param path path
     * @param jzonbieRouteContextConsumer route context consumer
     * @return HTTP POST route for path
     */
    public static JzonbieRoute post(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(POST, path, jzonbieRouteContextConsumer);
    }

    /**
     * Returns a HTTP PUT JzonbieRoute for the given path.
     *
     * @param path path
     * @param jzonbieRouteContextConsumer route context consumer
     * @return HTTP PUT route for path
     */
    public static JzonbieRoute put(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(PUT, path, jzonbieRouteContextConsumer);
    }

    /**
     * HTTP Methods.
     */
    public enum Method {
        ANY, CONNECT, DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT
    }

    /**
     * Class that provides access to the current {@link RouteContext}, {@link Deserializer},
     * and {@link JzonbieClient} for the current {@link Jzonbie} instance.
     */
    public static class JzonbieRouteContext {
        private final JzonbieClient jzonbieClient;
        private final Deserializer deserializer;
        private final RouteContext routeContext;

        JzonbieRouteContext(JzonbieClient jzonbieClient, Deserializer deserializer, RouteContext routeContext) {
            this.jzonbieClient = jzonbieClient;
            this.deserializer = deserializer;
            this.routeContext = routeContext;
        }

        /**
         * Returns a {@code JzonbieClient} for the current Jzonbie instance.
         *
         * @return jzonbie client
         */
        public JzonbieClient getJzonbieClient() {
            return jzonbieClient;
        }

        /**
         * Returns the currently configured {@link Deserializer}.
         *
         * @return deserializer
         */
        public Deserializer getDeserializer() {
            return deserializer;
        }

        /**
         * Returns the current {@link RouteContext}.
         *
         * @return route context
         */
        public RouteContext getRouteContext() {
            return routeContext;
        }
    }
}