package com.jonnymatts.jzonbie.pippo;

import com.jonnymatts.jzonbie.client.JzonbieClient;
import com.jonnymatts.jzonbie.util.Deserializer;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.Routing;

import java.util.function.Consumer;

import static com.jonnymatts.jzonbie.pippo.JzonbieRoute.Method.*;

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

    public static JzonbieRoute route(Method method, String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(method, path, jzonbieRouteContextConsumer);
    }

    public static JzonbieRoute any(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(ANY, path, jzonbieRouteContextConsumer);
    }

    public static JzonbieRoute connect(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(CONNECT, path, jzonbieRouteContextConsumer);
    }

    public static JzonbieRoute delete(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(DELETE, path, jzonbieRouteContextConsumer);
    }

    public static JzonbieRoute get(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(GET, path, jzonbieRouteContextConsumer);
    }

    public static JzonbieRoute head(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(HEAD, path, jzonbieRouteContextConsumer);
    }

    public static JzonbieRoute options(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(OPTIONS, path, jzonbieRouteContextConsumer);
    }

    public static JzonbieRoute patch(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(PATCH, path, jzonbieRouteContextConsumer);
    }

    public static JzonbieRoute post(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(POST, path, jzonbieRouteContextConsumer);
    }

    public static JzonbieRoute put(String path, Consumer<JzonbieRouteContext> jzonbieRouteContextConsumer) {
        return new JzonbieRoute(PUT, path, jzonbieRouteContextConsumer);
    }

    public enum Method {
        ANY, CONNECT, DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT
    }

    public static class JzonbieRouteContext {
        private final JzonbieClient jzonbieClient;
        private final Deserializer deserializer;
        private final RouteContext routeContext;

        public JzonbieRouteContext(JzonbieClient jzonbieClient, Deserializer deserializer, RouteContext routeContext) {
            this.jzonbieClient = jzonbieClient;
            this.deserializer = deserializer;
            this.routeContext = routeContext;
        }

        public JzonbieClient getJzonbieClient() {
            return jzonbieClient;
        }

        public Deserializer getDeserializer() {
            return deserializer;
        }

        public RouteContext getRouteContext() {
            return routeContext;
        }
    }
}