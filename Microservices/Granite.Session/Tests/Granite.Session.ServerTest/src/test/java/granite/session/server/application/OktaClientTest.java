//////////////////////////////////////////////////////////////////////////////
// OktaClientTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class OktaClientTest
{
    private static final String SUB   = "00u1abcd";
    private static final String NONCE = "nonce-value-123";

    private FakeOkta   okta;
    private OktaClient subject;

    @BeforeEach
    public void
    setUp()
    {
        okta    = new FakeOkta();
        subject = okta.client(okta.settings(),Clock.systemUTC());
    }

    @AfterEach
    public void
    tearDown()
    {
        okta.stop();
    }

    @Test
    public void
    discoveryIsFetchedOnceAndCached()
    {
        subject.buildAuthorizeUrl("s1","n1","c1",false);
        subject.buildAuthorizeUrl("s2","n2","c2",false);
        subject.buildEndSessionUrl("x.y.z");

        assertEquals(1,okta.discoveryRequests());
    }

    @Test
    public void
    discoveryWithAnotherIssuerIsRejected()
    {
        okta.stubDiscovery("https://evil.example.com/oauth2/default");

        assertThrows(OktaLoginException.class,() -> subject.getEndpoints());
    }

    @Test
    public void
    authorizeUrlCarriesTheCodeFlowParameters()
    {
        UriComponents url    = parse(subject.buildAuthorizeUrl("state-1","nonce-1","challenge-1",false));
        Map<String,String> q = url.getQueryParams().toSingleValueMap();

        assertEquals(okta.url(FakeOkta.AUTHORIZE_PATH),url.getScheme() + "://" + url.getHost() + ":" + url.getPort() + url.getPath());
        assertEquals("code",q.get("response_type"));
        assertEquals(FakeOkta.CLIENT_ID,q.get("client_id"));
        assertEquals(FakeOkta.REDIRECT_URI,decode(q.get("redirect_uri")));
        assertEquals("openid profile email groups",decode(q.get("scope")));
        assertEquals("state-1",q.get("state"));
        assertEquals("nonce-1",q.get("nonce"));
        assertEquals("challenge-1",q.get("code_challenge"));
        assertEquals("S256",q.get("code_challenge_method"));
        assertFalse(q.containsKey("prompt"));
    }

    @Test
    public void
    authorizeUrlAsksForLoginWhenRequested()
    {
        Map<String,String> q =
            parse(subject.buildAuthorizeUrl("s","n","c",true)).getQueryParams().toSingleValueMap();

        assertEquals("login",q.get("prompt"));
    }

    @Test
    public void
    exchangePostsTheCodeWithAPrivateKeyJwtAssertion()
        throws Exception
    {
        okta.stubTokenReturning(okta.idToken(SUB,NONCE));

        subject.exchange("code-1","verifier-1",NONCE);

        Map<String,String> form = okta.tokenRequests().get(0);

        assertTrue(okta.tokenRequestContentTypes().get(0).startsWith("application/x-www-form-urlencoded"));
        assertEquals("authorization_code",form.get("grant_type"));
        assertEquals("code-1",form.get("code"));
        assertEquals("verifier-1",form.get("code_verifier"));
        assertEquals(FakeOkta.REDIRECT_URI,form.get("redirect_uri"));
        assertEquals(FakeOkta.CLIENT_ID,form.get("client_id"));
        assertEquals("urn:ietf:params:oauth:client-assertion-type:jwt-bearer",form.get("client_assertion_type"));
        assertFalse(form.containsKey("client_secret"));

        SignedJWT    assertion = SignedJWT.parse(form.get("client_assertion"));
        JWTClaimsSet claims    = assertion.getJWTClaimsSet();
        Instant      now       = Instant.now();

        assertEquals(JWSAlgorithm.RS256,assertion.getHeader().getAlgorithm());
        assertEquals(okta.getClientKey().getKeyID(),assertion.getHeader().getKeyID());
        assertTrue(assertion.verify(new RSASSAVerifier(okta.getClientKey().toRSAPublicKey())));
        assertEquals(FakeOkta.CLIENT_ID,claims.getIssuer());
        assertEquals(FakeOkta.CLIENT_ID,claims.getSubject());
        assertEquals(List.of(okta.url(FakeOkta.TOKEN_PATH)),claims.getAudience());
        assertTrue(claims.getJWTID() != null && !claims.getJWTID().isBlank());
        assertTrue(claims.getExpirationTime().toInstant().isAfter(now));
        assertFalse(claims.getExpirationTime().toInstant().isAfter(now.plusSeconds(301)));
    }

    @Test
    public void
    exchangeReturnsTheValidatedIdentity()
    {
        String idToken = okta.idToken(SUB,NONCE);

        okta.stubTokenReturning(idToken);

        OktaIdentity identity = subject.exchange("code-1","verifier-1",NONCE);

        assertEquals(SUB,identity.getSubject());
        assertEquals(SUB + "@example.com",identity.getUserName());
        assertEquals(Set.of("Granite Admins","Everyone"),identity.getGroups());
        assertEquals(idToken,identity.getIdToken());
        assertFalse(identity.toString().contains(idToken));
    }

    @Test
    public void
    userNameFallsBackToEmailThenSubject()
    {
        okta.stubTokenReturning(
            okta.idToken(SUB,NONCE,builder -> builder.claim("preferred_username",null)));
        assertEquals(SUB + "@example.com",subject.exchange("c","v",NONCE).getUserName());

        okta.stubTokenReturning(
            okta.idToken(SUB,NONCE,builder -> builder.claim("preferred_username",null).claim("email",null)));
        assertEquals("okta:" + SUB,subject.exchange("c","v",NONCE).getUserName());
    }

    @Test
    public void
    missingGroupsClaimGivesNoGroups()
    {
        okta.stubTokenReturning(okta.idToken(SUB,NONCE,builder -> builder.claim("groups",null)));

        assertTrue(subject.exchange("c","v",NONCE).getGroups().isEmpty());
    }

    @Test
    public void
    wrongNonceIsRejected()
    {
        okta.stubTokenReturning(okta.idToken(SUB,"other-nonce"));

        assertThrows(OktaLoginException.class,() -> subject.exchange("c","v",NONCE));
    }

    @Test
    public void
    missingNonceIsRejected()
    {
        okta.stubTokenReturning(okta.idToken(SUB,null));

        assertThrows(OktaLoginException.class,() -> subject.exchange("c","v",NONCE));
    }

    @Test
    public void
    wrongAudienceIsRejected()
    {
        okta.stubTokenReturning(okta.idToken(SUB,NONCE,builder -> builder.audience("someone-else")));

        assertThrows(OktaLoginException.class,() -> subject.exchange("c","v",NONCE));
    }

    @Test
    public void
    wrongIssuerIsRejected()
    {
        okta.stubTokenReturning(
            okta.idToken(SUB,NONCE,builder -> builder.issuer("https://evil.example.com/oauth2/default")));

        assertThrows(OktaLoginException.class,() -> subject.exchange("c","v",NONCE));
    }

    @Test
    public void
    expiredTokenIsRejected()
    {
        Instant past = Instant.now().minusSeconds(3600);

        okta.stubTokenReturning(
            okta.idToken(
                SUB,
                NONCE,
                builder -> builder.issueTime(Date.from(past)).expirationTime(Date.from(past.plusSeconds(300)))));

        assertThrows(OktaLoginException.class,() -> subject.exchange("c","v",NONCE));
    }

    @Test
    public void
    missingExpirationIsRejected()
    {
        okta.stubTokenReturning(okta.idToken(SUB,NONCE,builder -> builder.expirationTime(null)));

        assertThrows(OktaLoginException.class,() -> subject.exchange("c","v",NONCE));
    }

    @Test
    public void
    foreignSignatureIsRejected()
    {
        RSAKey foreign =
            new RSAKey.Builder(okta.generate()).keyID(okta.getSigningKey().getKeyID()).build();

        okta.stubTokenReturning(okta.sign(foreign,okta.claims(SUB,NONCE,builder -> {})));

        assertThrows(OktaLoginException.class,() -> subject.exchange("c","v",NONCE));
    }

    @Test
    public void
    unsignedTokenIsRejected()
    {
        okta.stubTokenReturning(new PlainJWT(okta.claims(SUB,NONCE,builder -> {})).serialize());

        assertThrows(OktaLoginException.class,() -> subject.exchange("c","v",NONCE));
    }

    @Test
    public void
    tokenEndpointErrorIsRejected()
    {
        okta.stubTokenError();

        assertThrows(OktaLoginException.class,() -> subject.exchange("c","v",NONCE));
    }

    @Test
    public void
    exceptionMessagesNeverCarryTheCodeOrVerifier()
    {
        okta.stubTokenError();

        OktaLoginException e =
            assertThrows(OktaLoginException.class,() -> subject.exchange("secret-code","secret-verifier",NONCE));

        assertFalse(String.valueOf(e.getMessage()).contains("secret-code"));
        assertFalse(String.valueOf(e.getMessage()).contains("secret-verifier"));
    }

    @Test
    public void
    endSessionUrlCarriesTheHintAndPostLogoutRedirect()
    {
        Map<String,String> q =
            parse(subject.buildEndSessionUrl("id.token.value").orElseThrow())
                .getQueryParams()
                .toSingleValueMap();

        assertEquals("id.token.value",q.get("id_token_hint"));
        assertEquals(FakeOkta.POST_LOGOUT_REDIRECT_URI,decode(q.get("post_logout_redirect_uri")));
        assertTrue(subject.buildEndSessionUrl("id.token.value").orElseThrow().startsWith(okta.url(FakeOkta.LOGOUT_PATH)));
    }

    @Test
    public void
    endSessionUrlOmitsPostLogoutRedirectWhenUnset()
    {
        OktaClient client = okta.client(okta.settings().setPostLogoutRedirectUri(null),Clock.systemUTC());

        Map<String,String> q =
            parse(client.buildEndSessionUrl("id.token.value").orElseThrow()).getQueryParams().toSingleValueMap();

        assertNull(q.get("post_logout_redirect_uri"));
        assertTrue(subject.buildEndSessionUrl(null).isEmpty());
    }

    private UriComponents
    parse(String url)
    {
        return UriComponentsBuilder.fromUriString(url).build();
    }

    private String
    decode(String value)
    {
        return value == null ? null : URLDecoder.decode(value,StandardCharsets.UTF_8);
    }
}

//////////////////////////////////////////////////////////////////////////////
