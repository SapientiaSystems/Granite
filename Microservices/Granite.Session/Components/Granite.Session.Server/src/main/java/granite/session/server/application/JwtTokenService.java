//////////////////////////////////////////////////////////////////////////////
// JwtTokenService.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import granite.session.server.domain.Session;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public
class JwtTokenService
    implements IJwtTokenService
{
    private static final String DEVICE_ID_CLAIM   = "did";
    private static final String AUTHORITIES_CLAIM = "authorities";

    private final RSASSASigner    signer;
    private final RSASSAVerifier  verifier;
    private final String          keyId;
    private final String          issuer;
    private final List<String>    audience;
    private final Duration        ttl;

    public
    JwtTokenService(
        RSAPrivateKey privateKey,
        RSAPublicKey  publicKey,
        String        issuer,
        Collection<String> audience,
        Duration      ttl)
    {
        Objects.requireNonNull(privateKey,"privateKey");
        Objects.requireNonNull(publicKey,"publicKey");
        Objects.requireNonNull(issuer,"issuer");
        Objects.requireNonNull(audience,"audience");
        if (audience.isEmpty())
            throw new IllegalArgumentException("audience must not be empty");
        Objects.requireNonNull(ttl,"ttl");

        this.signer   = new RSASSASigner(privateKey);
        this.verifier = new RSASSAVerifier(publicKey);
        this.keyId    = computeKeyId(publicKey);
        this.issuer   = issuer;
        this.audience = List.copyOf(audience);
        this.ttl      = ttl;
    }

    @Override
    public String
    mint(Session session)
    {
        Objects.requireNonNull(session,"session");
        Objects.requireNonNull(session.getUserId(),"session.userId");
        Objects.requireNonNull(session.getSessionId(),"session.sessionId");

        Instant      now         = Instant.now();
        Instant      exp         = computeExpiration(session.getExpires(),now);
        Set<String>  authorities = session.getAuthorities();
        JWTClaimsSet claims      =
            new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(String.valueOf(session.getUserId()))
                .jwtID(String.valueOf(session.getSessionId()))
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .audience(audience)
                .claim(DEVICE_ID_CLAIM,session.getDeviceId())
                .claim(
                    AUTHORITIES_CLAIM,
                    authorities != null ? new ArrayList<>(authorities) : List.of())
                .build();
        return sign(claims);
    }

    @Override
    public String
    mintServiceToken(String subject,Collection<String> authorities,Duration lifetime)
    {
        Objects.requireNonNull(subject,"subject");
        Objects.requireNonNull(authorities,"authorities");
        Objects.requireNonNull(lifetime,"lifetime");

        Instant      now    = Instant.now();
        JWTClaimsSet claims =
            new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(subject)
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(lifetime)))
                .audience(audience)
                .claim(AUTHORITIES_CLAIM,new ArrayList<>(authorities))
                .build();

        return sign(claims);
    }

    @Override
    public JwtVerificationResult
    verifySignatureAndExp(String jwt)
        throws JwtVerificationException
    {
        Objects.requireNonNull(jwt,"jwt");

        SignedJWT    parsed;
        JWTClaimsSet claims;

        try
        {
            parsed = SignedJWT.parse(jwt);
        }
        catch (ParseException e)
        {
            throw new JwtVerificationException("parse failure: " + e.getMessage(),e);
        }

        try
        {
            if (!parsed.verify(verifier))
                throw new JwtVerificationException("bad signature");
        }
        catch (JOSEException e)
        {
            throw new JwtVerificationException("signature verification failure: " + e.getMessage(),e);
        }

        try
        {
            claims = parsed.getJWTClaimsSet();
        }
        catch (ParseException e)
        {
            throw new JwtVerificationException("claims parse failure: " + e.getMessage(),e);
        }

        Date expirationTime = claims.getExpirationTime();

        if (expirationTime == null)
            throw new JwtVerificationException("missing exp claim");

        Instant exp = expirationTime.toInstant();

        if (!exp.isAfter(Instant.now()))
            throw new JwtVerificationException("expired at " + exp);

        if (!issuer.equals(claims.getIssuer()))
            throw new JwtVerificationException("bad issuer: " + claims.getIssuer());

        List<String> aud = claims.getAudience();

        if (aud == null || Collections.disjoint(aud,audience))
            throw new JwtVerificationException("bad audience: " + aud);

        Date issueTime = claims.getIssueTime();

        return
            new JwtVerificationResult()
                .setSubject(claims.getSubject())
                .setJwtId(claims.getJWTID())
                .setIssuedAt(issueTime != null ? issueTime.toInstant() : null)
                .setExpiresAt(exp)
                .setAudience(aud)
                .setDeviceId(readStringClaim(claims,DEVICE_ID_CLAIM))
                .setAuthorities(readStringListClaim(claims,AUTHORITIES_CLAIM));
    }

    @Override
    public Instant
    getExpiration(String jwt)
    {
        if (jwt == null || jwt.isBlank())
            return null;

        try
        {
            Date expirationTime = SignedJWT.parse(jwt).getJWTClaimsSet().getExpirationTime();

            return expirationTime != null ? expirationTime.toInstant() : null;
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    public String
    getKeyId() { return keyId; }

    private String
    sign(JWTClaimsSet claims)
    {
        SignedJWT signed =
            new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .keyID(keyId)
                    .build(),
                claims);

        try
        {
            signed.sign(signer);
        }
        catch (JOSEException e)
        {
            throw new IllegalStateException("failed to sign JWT",e);
        }

        return signed.serialize();
    }

    private Instant
    computeExpiration(Instant sessionExpires,Instant now)
    {
        Instant capped = now.plus(ttl);

        if (sessionExpires != null && sessionExpires.isBefore(capped))
            return sessionExpires;

        return capped;
    }

    private static String
    computeKeyId(RSAPublicKey publicKey)
    {
        try
        {
            return
                new RSAKey.Builder(publicKey)
                    .keyIDFromThumbprint()
                    .build()
                    .getKeyID();
        }
        catch (JOSEException e)
        {
            throw new IllegalStateException("failed to compute key id",e);
        }
    }

    private String
    readStringClaim(JWTClaimsSet claims,String name)
    {
        try
        {
            return claims.getStringClaim(name);
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    private Set<String>
    readStringListClaim(JWTClaimsSet claims,String name)
    {
        try
        {
            List<String> values = claims.getStringListClaim(name);

            if (values == null)
                return Set.of();

            return new java.util.LinkedHashSet<>(values);
        }
        catch (ParseException e)
        {
            return Set.of();
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
