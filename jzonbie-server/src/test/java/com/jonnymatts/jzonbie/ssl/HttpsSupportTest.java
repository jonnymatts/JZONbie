package com.jonnymatts.jzonbie.ssl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sun.security.x509.X509CertImpl;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class HttpsSupportTest {

    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private String keystorePath;

    @Before
    public void setUp() throws Exception {
        keystorePath = temporaryFolder.getRoot().getAbsolutePath() + "/keystore.jks";
    }

    @Test
    public void generateKeystoreGeneratesKeystoreWithCorrectCertificate() throws Exception {
        HttpsSupport.createKeystoreAndTruststore(keystorePath, "localhost");

        final KeyStore generatedKeystore = KeyStore.getInstance("jks");
        generatedKeystore.load(new FileInputStream(keystorePath), "jzonbie".toCharArray());

        final Certificate certificate = generatedKeystore.getCertificate("jzonbie");

        assertThat(new X509CertImpl(certificate.getEncoded()).getSubjectDN().getName()).isEqualTo("CN=localhost");
    }

    @Test
    public void generateKeystoreAndTruststoreThrowsExceptionIfKeystoreDestinationDirectoryDoesNotExist() {
        final String nonExistingPath = "/hbewfhjbfewhj/keystore.jks";

        assertThatThrownBy(() -> HttpsSupport.createKeystoreAndTruststore(nonExistingPath, "localhost"))
                .hasMessageContaining(nonExistingPath);
    }

    @Test
    public void getTruststoreThrowsExceptionIfTruststoreHasNotBeenCreatedYet() {
        assertThatThrownBy(HttpsSupport::getTrustStore)
                .hasMessageContaining("Truststore not created");

    }

    @Test
    public void getTruststoreOnceCreated() throws Exception {
        HttpsSupport.createKeystoreAndTruststore(keystorePath, "localhost");

        final KeyStore trustStore = HttpsSupport.getTrustStore();

        final Certificate certificate = trustStore.getCertificate("jzonbie");

        assertThat(new X509CertImpl(certificate.getEncoded()).getSubjectDN().getName()).isEqualTo("CN=localhost");
    }

    @Test
    public void getTruststoreAsByteArray() throws Exception {
        HttpsSupport.createKeystoreAndTruststore(keystorePath, "localhost");

        final KeyStore keystore = KeyStore.getInstance("jks");
        final byte[] bytes = HttpsSupport.getTrustStoreAsByteArray();

        keystore.load(new ByteArrayInputStream(bytes), new char[0]);

        final Certificate certificate = keystore.getCertificate("jzonbie");

        assertThat(new X509CertImpl(certificate.getEncoded()).getSubjectDN().getName()).isEqualTo("CN=localhost");
    }
}