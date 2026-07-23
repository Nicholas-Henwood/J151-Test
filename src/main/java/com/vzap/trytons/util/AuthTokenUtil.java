package com.vzap.trytons.util;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.vzap.trytons.config.DotEnvConfig;
import com.vzap.trytons.exceptions.AuthenticationException;
import com.vzap.trytons.exceptions.ValidationException;
import com.vzap.trytons.security.AuthTokenPayload;

import java.util.Date;
import java.util.UUID;

public class AuthTokenUtil {

    private static final String ISSUER = DotEnvConfig.getRequired("AUTH_TOKEN_ISSUER");
    private static final String SECRET = DotEnvConfig.getRequired("AUTH_TOKEN_SECRET");
    private static final long TOKEN_LIFETIME_MILLIS =DotEnvConfig.getRequiredLong("AUTH_TOKEN_LIFETIME_MILLIS");

    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);
    private static final JWTVerifier VERIFIER = JWT.require(ALGORITHM).withIssuer(ISSUER).build();

    private AuthTokenUtil(){
    }

    public static String createToken (UUID userId) throws ValidationException {
        if (userId == null) {
            throw new ValidationException("UserID cannot be null when generating a token.");
        }
        Date issuedAt = new Date();
        Date expiresAt = calculateExpiryDate(issuedAt);

        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(userId.toString())
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .sign(ALGORITHM);
    }

    public static AuthTokenPayload validateToken(String token) {
        if (token == null || token.isBlank()){
            throw new ValidationException("Token cannot be null or blank");
        }

        try {
            DecodedJWT decodedJWT = VERIFIER.verify(token);
            String userId = decodedJWT.getSubject();
            if (userId == null || userId.isBlank()){
                throw new AuthenticationException("Token does not contain a valid ID");
            }
            UUID validUserId;
            try{
                validUserId = UUID.fromString(userId);
            }catch (IllegalArgumentException e){
                throw new AuthenticationException("Token does not contain a valid ID", e);
            }

            Date expiresAt = decodedJWT.getExpiresAt();
            if (expiresAt == null) {
                throw new AuthenticationException("Authentication token does not contain an expiry date.");
            }

            return AuthTokenPayload.builder()
                    .userId(validUserId)
                    .expiresAt(expiresAt)
                    .build();

        } catch (JWTVerificationException e) {
            throw new AuthenticationException("Authentication token is invalid.", e);
        }
    }

    public static String refreshToken(String token) {
        AuthTokenPayload payload = validateToken(token);
        return createToken(payload.getUserId());
    }

    public static void revokeToken(String token) {
        /*
        No server-side token revocation is available until
        refresh-token storage or a token denylist is implemented.
    */
    }

    private static Date calculateExpiryDate(Date issuedAt) {
        long expirationTime = issuedAt.getTime() + TOKEN_LIFETIME_MILLIS;
        return new Date(expirationTime);
    }
}
