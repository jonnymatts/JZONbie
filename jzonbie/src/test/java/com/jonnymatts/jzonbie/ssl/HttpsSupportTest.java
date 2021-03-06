package com.jonnymatts.jzonbie.ssl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sun.security.x509.X509CertImpl;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class HttpsSupportTest {

    @TempDir static Path temporaryFolder;

    private String keystorePath;

    private HttpsSupport underTest;

    @BeforeEach
    void setUp() throws Exception {
        underTest = new HttpsSupport();

        keystorePath = temporaryFolder.toAbsolutePath().toString() + "/keystore.jks";
    }

    @Test
    void generateKeystoreGeneratesKeystoreWithCorrectCertificate() throws Exception {
        underTest.createKeystoreAndTruststore(keystorePath, "localhost");

        final KeyStore generatedKeystore = KeyStore.getInstance("jks");
        generatedKeystore.load(new FileInputStream(keystorePath), "jzonbie".toCharArray());

        final Certificate certificate = generatedKeystore.getCertificate("jzonbie");

        assertThat(new X509CertImpl(certificate.getEncoded()).getSubjectDN().getName()).isEqualTo("CN=localhost");
    }

    @Test
    void generateKeystoreAndTruststoreThrowsExceptionIfKeystoreDestinationDirectoryDoesNotExist() {
        final String nonExistingPath = "/hbewfhjbfewhj/keystore.jks";

        assertThatThrownBy(() -> underTest.createKeystoreAndTruststore(nonExistingPath, "localhost"))
                .hasMessageContaining(nonExistingPath);
    }

    @Test
    void getTruststoreThrowsExceptionIfTruststoreHasNotBeenCreatedYet() {
        assertThatThrownBy(underTest::getTrustStore)
                .hasMessageContaining("Truststore not created");

    }

    @Test
    void getTruststoreOnceCreated() throws Exception {
        underTest.createKeystoreAndTruststore(keystorePath, "localhost");

        final KeyStore trustStore = underTest.getTrustStore();

        final Certificate certificate = trustStore.getCertificate("jzonbie");

        assertThat(new X509CertImpl(certificate.getEncoded()).getSubjectDN().getName()).isEqualTo("CN=localhost");
    }

    @Test
    void getTruststoreAsByteArray() throws Exception {
        underTest.createKeystoreAndTruststore(keystorePath, "localhost");

        final KeyStore keystore = KeyStore.getInstance("jks");
        final byte[] bytes = underTest.getTrustStoreAsByteArray();

        keystore.load(new ByteArrayInputStream(bytes), new char[0]);

        final Certificate certificate = keystore.getCertificate("jzonbie");

        assertThat(new X509CertImpl(certificate.getEncoded()).getSubjectDN().getName()).isEqualTo("CN=localhost");
    }
}