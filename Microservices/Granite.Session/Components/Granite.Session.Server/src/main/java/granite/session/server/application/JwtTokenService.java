//////////////////////////////////////////////////////////////////////////////
// JwtTokenService.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import granite.session.server.domain.Session;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public
class JwtTokenService
    implements IJwtTokenService
{
    private static final String DEVICE_ID_CLAIM = "did";

    private final RSASSASigner    signer;
    private final RSASSAVerifier  verifier;
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

        Instant      now       = Instant.now();
        Instant      exp       = session.getExpires() != null ? session.getExpires() : now.plus(ttl);
        JWTClaimsSet claims    =
            new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(String.valueOf(session.getUserId()))
                .jwtID(String.valueOf(session.getSessionId()))
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .audience(audience)
                .claim(DEVICE_ID_CLAIM,session.getDeviceId())
                .build();
        SignedJWT    signed    = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256),claims);

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
                .setDeviceId(readStringClaim(claims,DEVICE_ID_CLAIM));
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
}

//////////////////////////////////////////////////////////////////////////////
