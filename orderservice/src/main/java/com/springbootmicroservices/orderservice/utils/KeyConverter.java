package com.springbootmicroservices.orderservice.utils;

import lombok.experimental.UtilityClass;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
// import org.slf4j.Logger; // Optional: for more detailed logging if desired
// import org.slf4j.LoggerFactory; // Optional

import java.io.IOException;
import java.io.StringReader;
// PrivateKey import is only needed if the convertPrivateKey method is active
// import java.security.PrivateKey;
import java.security.PublicKey;

@UtilityClass
public class KeyConverter {

    // Optional: Logger if you want to log more details during conversion
    // private static final Logger log = LoggerFactory.getLogger(KeyConverter.class);

    /**
     * Converts a PEM-encoded public key string to a {@link PublicKey} object.
     *
     * @param publicPemKey the PEM-encoded public key string
     * @return the corresponding {@link PublicKey} object
     * @throws IllegalArgumentException if the publicPemKey string is null, blank, or invalid PEM format.
     * @throws RuntimeException if an IOException occurs during parsing or conversion.
     */
    public PublicKey convertPublicKey(final String publicPemKey) {
        if (publicPemKey == null || publicPemKey.isBlank()) {
            // log.error("Public PEM key string cannot be null or empty."); // Optional logging
            throw new IllegalArgumentException("Public PEM key string cannot be null or empty.");
        }

        // log.debug("Attempting to convert public key from PEM string starting with: {}",
        //    publicPemKey.substring(0, Math.min(publicPemKey.length(), 50))); // Log snippet for debugging

        final StringReader keyReader = new StringReader(publicPemKey);
        try (PEMParser pemParser = new PEMParser(keyReader)) { // try-with-resources ensures pemParser is closed
            Object parsedObject = pemParser.readObject();

            if (parsedObject == null) {
                // log.error("Invalid PEM public key string: no object found after parsing.");
                throw new IllegalArgumentException("Invalid PEM public key string: no object found after parsing.");
            }

            if (!(parsedObject instanceof SubjectPublicKeyInfo)) {
                // log.error("Invalid PEM public key string: expected SubjectPublicKeyInfo, but found: {}",
                //    parsedObject.getClass().getName());
                throw new IllegalArgumentException("Invalid PEM public key string: not a SubjectPublicKeyInfo. Found: " + parsedObject.getClass().getName());
            }

            SubjectPublicKeyInfo publicKeyInfo = (SubjectPublicKeyInfo) parsedObject;
            PublicKey publicKey = new JcaPEMKeyConverter().getPublicKey(publicKeyInfo);
            // log.debug("Successfully converted PEM string to PublicKey of type: {}", publicKey.getAlgorithm());
            return publicKey;
        } catch (IOException e) {
            // log.error("IOException during PEM parsing or key conversion for public key.", e);
            throw new RuntimeException("Failed to convert public key from PEM string due to IO error.", e);
        } catch (Exception e) { // Catch other potential runtime exceptions from BouncyCastle or JCA
            // log.error("Unexpected error during public key conversion.", e);
            throw new RuntimeException("Unexpected error converting public key from PEM string.", e);
        }
    }

    // The OrderService, as planned, only validates tokens (using a public key) and does not issue them.
    // Therefore, it does not need to convert or use a private key.
    // This method can remain commented out or be removed for OrderService.
    /*
    public PrivateKey convertPrivateKey(final String privatePemKey) {
        if (privatePemKey == null || privatePemKey.isBlank()) {
            throw new IllegalArgumentException("Private PEM key string cannot be null or empty.");
        }
        final StringReader keyReader = new StringReader(privatePemKey);
        try (PEMParser pemParser = new PEMParser(keyReader)) {
             Object parsedObject = pemParser.readObject();
            if (parsedObject == null) {
                throw new IllegalArgumentException("Invalid PEM private key string: no object found after parsing.");
            }
            if (!(parsedObject instanceof PrivateKeyInfo)) {
                 throw new IllegalArgumentException("Invalid PEM private key string: not a PrivateKeyInfo. Found: " + parsedObject.getClass().getName());
            }
            PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) parsedObject;
            return new JcaPEMKeyConverter().getPrivateKey(privateKeyInfo);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert private key from PEM string due to IO error.", e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error converting private key from PEM string.", e);
        }
    }
    */
}