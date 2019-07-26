package com.jonnymatts.jzonbie;

import java.util.Optional;

public class HttpsOptions {
    public static final int DEFAULT_PORT = 0;
    private static final String DEFAULT_COMMON_NAME = "localhost";

    private int port;
    private String keystoreLocation;
    private String keystorePassword;
    private String commonName;

    public static HttpsOptions httpsOptions() {
        return new HttpsOptions();
    }

    private HttpsOptions() {
        this.port = DEFAULT_PORT;
        this.commonName = DEFAULT_COMMON_NAME;
    }

    public HttpsOptions withPort(int port) {
        this.port = port;
        return this;
    }

    public HttpsOptions withKeystoreLocation(String keystoreLocation) {
        this.keystoreLocation = keystoreLocation;
        return this;
    }

    public HttpsOptions withKeystorePassword(String password) {
        this.keystorePassword = password;
        return this;
    }

    public HttpsOptions withCommonName(String commonName) {
        this.commonName = commonName;
        return this;
    }

    public int getPort() {
        return port;
    }

    public Optional<String> getKeystoreLocation() {
        return Optional.ofNullable(keystoreLocation);
    }

    public Optional<String> getKeystorePassword() {
        return Optional.ofNullable(keystorePassword);
    }

    public String getCommonName() {
        return commonName;
    }
}
