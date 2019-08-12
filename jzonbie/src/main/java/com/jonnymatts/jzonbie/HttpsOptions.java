package com.jonnymatts.jzonbie;

import java.util.Optional;

/**
 * Class that defines the HTTPS configuration of a custom Jzonbie.
 * <p>
 * By default when HTTPS is requested, Jzonbie will be configured with
 * a self-signed certificate for "localhost". A JKS keystore containing
 * this certificate can be found at /tmp/jzonbie.jks.
 * The common name of this certificate can configured if the Jzonbie will
 * be hosted somewhere other than localhost.
 * <pre>
 * {@code
 * httpsOptions().withCommonName("remote.jzonbie.com")
 * }
 * </pre>
 * Jzonbie can also be configured with an existing JKS keystore.
 * <pre>
 * {@code
 * httpsOptions()
 *      .withKeystoreLocation("/config/keystore.jks")
 *      .withKeystorePassword("password")
 * }
 * </pre>
 */
public class HttpsOptions {
    private static final int DEFAULT_PORT = 0;
    private static final String DEFAULT_COMMON_NAME = "localhost";

    private int port;
    private String keystoreLocation;
    private String keystorePassword;
    private String commonName;

    /**
     * Returns the default HTTPS configuration.
     *
     * @return default HTTPS configuration
     */
    public static HttpsOptions httpsOptions() {
        return new HttpsOptions();
    }

    private HttpsOptions() {
        this.port = DEFAULT_PORT;
        this.commonName = DEFAULT_COMMON_NAME;
    }

    /**
     * Configures Jzonbie to serve HTTPS traffic on the given port.
     * <p>
     * By default Jzonbie will choose a random, available port.
     *
     * @param port HTTPS port
     * @return this HTTPS configuration with a configured port
     */
    public HttpsOptions withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Configures Jzonbie with the given JKS keystore.
     *
     * @param keystoreLocation path to JKS keystore
     * @return this HTTPS configuration with a configured keystore
     */
    public HttpsOptions withKeystoreLocation(String keystoreLocation) {
        this.keystoreLocation = keystoreLocation;
        return this;
    }

    /**
     * Configures Jzonbie with the given password for a provided keystore.
     *
     * @param password keystore password
     * @return this HTTPS configuration with a configured keystore password
     */
    public HttpsOptions withKeystorePassword(String password) {
        this.keystorePassword = password;
        return this;
    }

    /**
     * Configures Jzonbie to generate a self-signed certificate with the given common name.
     * <p>
     * By default Jzonbie will use the common name "localhost"
     *
     * @param commonName path to JKS keystore
     * @return this HTTPS configuration with a configured keystore
     */
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
