package org.radarcns.integration.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dverbeec on 29/06/2017. Updated in Rest-Api by yatharthranjan on 14/11/2017.
 */
public class ManagementPortalOAuth2 {
    private static final Logger logger = LoggerFactory.getLogger(ManagementPortalOAuth2.class);

    private static final String[] SCOPES = {"SUBJECT.READ", "PROJECT.READ", "SOURCE.READ",
            "DEVICETYPE.READ", "MEASUREMENT.READ"};
    private static final String[] AUTHORITIES = {"ROLE_SYS_ADMIN", "ROLE_USER"};
    private static final String[] ROLES = {"PROJECT1:ROLE_PROJECT_ADMIN",
            "PROJECT2:ROLE_PARTICIPANT"};
    private static final String[] SOURCES = {};
    private static final String CLIENT = "unit_test";
    private static final String USER = "admin";
    private static final String ISS = "RADAR";
    private static final String JTI = "some-jwt-id";

    private static final String AUDIENCE = "res_RestApi";

    private final String publicKey;
    private final Algorithm algorithm;

    /**
     * Set up a keypair for signing the tokens, initialize the tokens for tests.
     *
     * @throws GeneralSecurityException If certificate signing is not possible
     * @throws IOException if keys cannot be loaded from file
     */
    public ManagementPortalOAuth2() throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream keyStream = ManagementPortalOAuth2.class
                .getClassLoader().getResourceAsStream("keystore.jks");
        ks.load(keyStream, "radarbase".toCharArray());
        RSAPrivateKey privateKey = (RSAPrivateKey) ks.getKey("selfsigned",
                "radarbase".toCharArray());
        Certificate cert = ks.getCertificate("selfsigned");
        RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();

        keyStream.close();

        this.publicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        this.algorithm = Algorithm.RSA256(publicKey, privateKey);
    }

    public String getAccessToken() {
        Instant exp = Instant.now().plusSeconds(3600);
        Instant iat = Instant.now().minusSeconds(30);

        String token = JWT.create()
                .withIssuer(ISS)
                .withIssuedAt(Date.from(iat))
                .withExpiresAt(Date.from(exp))
                .withAudience(AUDIENCE)
                .withSubject(USER)
                .withArrayClaim("scope", SCOPES)
                .withArrayClaim("authorities", AUTHORITIES)
                .withArrayClaim("roles", ROLES)
                .withArrayClaim("sources", SOURCES)
                .withClaim("client_id", CLIENT)
                .withClaim("user_name", USER)
                .withClaim("jti", JTI)
                .withClaim("grant_type", "refresh_token")
                .sign(algorithm);
        logger.info("Created token {}", token);
        return token;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
