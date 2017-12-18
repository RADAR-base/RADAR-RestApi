package org.radarcns.integration.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

/**
 * Created by dverbeec on 29/06/2017. Updated in Rest-Api by yatharthranjan on 14/11/2017.
 */
public class TokenTestUtils {

    public static String PUBLIC_KEY_BODY;
    public static String VALID_TOKEN;
    public static DecodedJWT SUPER_USER_TOKEN;

    public static final String[] SCOPES = {"SUBJECT.READ", "PROJECT.READ", "SOURCE.READ",
            "DEVICETYPE.READ", "MEASUREMENT.READ"};
    public static final String[] AUTHORITIES = {"ROLE_SYS_ADMIN", "ROLE_USER"};
    public static final String[] ROLES = {"PROJECT1:ROLE_PROJECT_ADMIN",
            "PROJECT2:ROLE_PARTICIPANT"};
    public static final String[] SOURCES = {};
    public static final String CLIENT = "unit_test";
    public static final String USER = "admin";
    public static final String ISS = "RADAR";
    public static final String JTI = "some-jwt-id";
    public static String PUBLIC_KEY_STRING;

    public static final String APPLICATION_JSON = "application/json";
    public static final String AUDIENCE = "res_RestApi";

    /**
     * Set up a keypair for signing the tokens, initialize the tokens for tests.
     *
     * @throws Exception If anything goes wrong during setup
     */
    public static void setUp() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream keyStream = TokenTestUtils.class
                .getClassLoader().getResourceAsStream("keystore.jks");
        ks.load(keyStream, "radarbase".toCharArray());
        RSAPrivateKey privateKey = (RSAPrivateKey) ks.getKey("selfsigned",
                "radarbase".toCharArray());
        Certificate cert = ks.getCertificate("selfsigned");
        RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();

        keyStream.close();

        PUBLIC_KEY_STRING = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        initVars(PUBLIC_KEY_STRING, Algorithm.RSA256(publicKey, privateKey));
    }

    private static void initVars(String publicKey, Algorithm algorithm) {
        PUBLIC_KEY_BODY = "{\n"
                + "  \"alg\" : \"SHA256withRSA\",\n"
                + "  \"value\" : \"-----BEGIN PUBLIC KEY-----\\n" + publicKey
                + "\\n-----END PUBLIC "
                + "KEY-----\"\n"
                + "}";

        Instant exp = Instant.now().plusSeconds(30 * 60);
        Instant iat = Instant.now();

        initValidToken(algorithm, exp, iat);
    }

    private static void initValidToken(Algorithm algorithm, Instant exp, Instant iat) {
        VALID_TOKEN = JWT.create()
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
                .sign(algorithm);
        SUPER_USER_TOKEN = JWT.decode(VALID_TOKEN);
    }
}
