package com.jonnymatts.jzonbie;

import com.google.common.io.Resources;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sun.security.x509.X509CertImpl;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;

import static com.jonnymatts.jzonbie.HttpsOptions.httpsOptions;
import static com.jonnymatts.jzonbie.JzonbieOptions.options;
import static com.jonnymatts.jzonbie.requests.AppRequest.get;
import static com.jonnymatts.jzonbie.responses.AppResponse.ok;
import static com.jonnymatts.jzonbie.responses.defaults.DefaultAppResponse.staticDefault;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

class JzonbieHttpsTest {

    private static final Jzonbie defaultHttpsJzonbie = new Jzonbie(options().withHttps());
    private static final Jzonbie configuredHttpsJzonbie = new Jzonbie(options().withHttps(httpsOptions().withKeystoreLocation(Resources.getResource("test.jks").toString()).withKeystorePassword("jzonbie")));

    private HttpUriRequest httpRequest;
    private HttpUriRequest defaultHttpsRequest;
    private HttpUriRequest configuredHttpsRequest;
    private HttpClient httpClient;
    private HttpClient httpsClient;

    @BeforeEach
    void setUp() throws Exception {
        httpRequest = RequestBuilder.get("http://localhost:" + defaultHttpsJzonbie.getHttpPort() + "/").build();
        defaultHttpsRequest = RequestBuilder.get("https://localhost:" + defaultHttpsJzonbie.getHttpsPort() + "/").build();
        configuredHttpsRequest = RequestBuilder.get("https://localhost:" + configuredHttpsJzonbie.getHttpsPort() + "/").build();

        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(10);
        connectionManager.setDefaultMaxPerRoute(10);
        httpClient = HttpClientBuilder.create().setConnectionManager(connectionManager).build();

        final SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(defaultHttpsJzonbie.getTruststore(), TrustSelfSignedStrategy.INSTANCE).build();
        final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        httpsClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
    }

    @AfterEach
    void tearDown() throws Exception {
        defaultHttpsJzonbie.reset();
        configuredHttpsJzonbie.reset();
    }

    @Test
    void jzonbieServesPrimedResponsesOverHttp() throws Exception {
        defaultHttpsJzonbie.prime(
                get("/"),
                staticDefault(ok())
        );

        final HttpResponse response = httpClient.execute(httpRequest);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
    }

    @Test
    void jzonbieServesPrimedResponsesOverDefaultHttps() throws Exception {
        defaultHttpsJzonbie.prime(
                get("/"),
                staticDefault(ok())
        );

        final HttpResponse response = httpsClient.execute(defaultHttpsRequest);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
    }

    @Test
    void jzonbieServesPrimedResponsesOverConfiguredHttps() throws Exception {
        configuredHttpsJzonbie.prime(
                get("/"),
                staticDefault(ok())
        );

        final HttpResponse response = httpsClient.execute(configuredHttpsRequest);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
    }

    @Test
    void jzonbieCanSetCommonNameOfDefaultSslCertificate() throws Exception {
        new Jzonbie(options().withHttps(HttpsOptions.httpsOptions().withCommonName("notLocalHost")));

        final KeyStore keystore = KeyStore.getInstance("jks");
        keystore.load(new FileInputStream("/tmp/jzonbie.jks"), "jzonbie".toCharArray());

        final Certificate certificate = keystore.getCertificate("jzonbie");

        assertThat(new X509CertImpl(certificate.getEncoded()).getSubjectDN().getName()).isEqualTo("CN=notLocalHost");
    }
}
