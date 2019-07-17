package com.jonnymatts.jzonbie.ssl;

import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static java.lang.String.format;

public class HttpsSupport {

    private static KeyStore trustStore;

    public static void createKeystoreAndTruststore(String keystorePath, String subject) {
        try {
            final CertAndPrivateKey certAndPrivateKey = generateCertificateAndPrivateKey(subject);
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null);
            final KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(certAndPrivateKey.privateKey, new Certificate[]{certAndPrivateKey.certificate});
            ks.setEntry("jzonbie", privateKeyEntry, new KeyStore.PasswordProtection("jzonbie".toCharArray()));
            final FileOutputStream fileOutputStream = new FileOutputStream(keystorePath);
            ks.store(fileOutputStream, "jzonbie".toCharArray());

            HttpsSupport.trustStore = createTruststore(certAndPrivateKey.certificate);
        } catch(Exception e) {
            throw new RuntimeException(format("Failed to generate jzonbie keystore: %s", keystorePath), e);
        }
    }

    public static KeyStore getTrustStore() {
        if(trustStore == null) {
            throw new RuntimeException("Truststore not created yet");
        }
        return trustStore;
    }

    public static byte[] getTrustStoreAsByteArray() {
        final KeyStore trustStore = getTrustStore();

        try(final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            trustStore.store(outputStream, new char[0]);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert truststore to byte array", e);
        }
    }

    private static CertAndPrivateKey generateCertificateAndPrivateKey(String subject) throws Exception {
        CertAndKeyGen gen = new CertAndKeyGen("RSA", "MD5WithRSA");
        gen.generate(2048);
        final X509Certificate certificate = gen.getSelfCertificate(new X500Name("CN = " + subject), 3_155_760_000L); // 100 years
        return new CertAndPrivateKey(certificate, gen.getPrivateKey());
    }

    private static KeyStore createTruststore(X509Certificate certificate) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null);
        ks.setCertificateEntry("jzonbie", certificate);
        return ks;
    }

    private static class CertAndPrivateKey {
        private final X509Certificate certificate;
        private final PrivateKey privateKey;

        private CertAndPrivateKey(X509Certificate certificate, PrivateKey privateKey) {
            this.certificate = certificate;
            this.privateKey = privateKey;
        }
    }
}
