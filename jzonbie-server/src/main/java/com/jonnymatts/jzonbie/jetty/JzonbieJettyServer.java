package com.jonnymatts.jzonbie.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import ro.pippo.jetty.JettyServer;

public class JzonbieJettyServer extends JettyServer {

    private Server server;

    public JzonbieJettyServer() {}

    @Override
    protected Server createServer() {
        server = super.createServer();

        return server;
    }

    @Override
    public int getPort() {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }
}