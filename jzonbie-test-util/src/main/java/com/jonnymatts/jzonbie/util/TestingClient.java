package com.jonnymatts.jzonbie.util;

import com.jonnymatts.jzonbie.requests.AppRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;

public class TestingClient {

    private final HttpClient client;
    private final String baseUrl;

    public TestingClient(String baseUrl) {
        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(10);
        connectionManager.setDefaultMaxPerRoute(10);
        this.client = HttpClientBuilder.create().setConnectionManager(connectionManager).build();
        this.baseUrl = baseUrl;
    }

    public void execute(AppRequest request) {
        final HttpUriRequest clientRequest = RequestBuilder.create(request.getMethod())
                .setUri(baseUrl + request.getPath())
                .build();

        try {
            client.execute(clientRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}