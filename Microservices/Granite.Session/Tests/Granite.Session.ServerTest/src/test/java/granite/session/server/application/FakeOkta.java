//////////////////////////////////////////////////////////////////////////////
// FakeOkta.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public
class FakeOkta
{
    public static final String CLIENT_ID                = "granite-test-client";
    public static final String REDIRECT_URI             = "https://app.example.com/login/callback";
    public static final String POST_LOGOUT_REDIRECT_URI = "https://app.example.com/";
    public static final String AUTH_SERVER_PATH         = "/oauth2/default";
    public static final String DISCOVERY_PATH           = AUTH_SERVER_PATH + "/.well-known/openid-configuration";
    public static final String TOKEN_PATH               = AUTH_SERVER_PATH + "/v1/token";
    public static final String JWKS_PATH                = AUTH_SERVER_PATH + "/v1/keys";
    public static final String AUTHORIZE_PATH           = AUTH_SERVER_PATH + "/v1/authorize";
    public static final String LOGOUT_PATH              = AUTH_SERVER_PATH + "/v1/logout";

    private final WireMockServer server;
    private final RSAKey         signingKey;
    private final RSAKey         clientKey;

    public
    FakeOkta()
    {
        this(0);
    }

    public
    FakeOkta(int port)
    {
        server     = new WireMockServer(port > 0 ? wireMockConfig().port(port) : wireMockConfig().dynamicPort());
        signingKey = generate();
        clientKey  = generate();
        server.start();
        stubDiscovery(getIssuer());
        stubJwks(signingKey);
    }

    public void
    stop() { server.stop(); }

    public String
    getIssuer() { return server.baseUrl() + AUTH_SERVER_PATH; }

    public String
    url(String path) { return server.baseUrl() + path; }

    public RSAKey
    getSigningKey() { return signingKey; }

    public RSAKey
    getClientKey() { return clientKey; }

    public OktaSettings
    settings()
    {
        return
            new OktaSettings()
                .setEnabled(true)
                .setIssuer(getIssuer())
                .setClientId(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .setPostLogoutRedirectUri(POST_LOGOUT_REDIRECT_URI);
    }

    public OktaClient
    client(OktaSettings settings,Clock clock)
    {
        try
        {
            return
                new OktaClient(
                    settings,
                    new OktaClientAssertionSigner(clientKey.toRSAPrivateKey(),clientKey.getKeyID(),clock),
                    clock);
        }
        catch (JOSEException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public void
    stubDiscovery(String issuer)
    {
        Map<String,Object> document = new LinkedHashMap<>();

        document.put("issuer",issuer);
        document.put("authorization_endpoint",url(AUTHORIZE_PATH));
        document.put("token_endpoint",url(TOKEN_PATH));
        document.put("jwks_uri",url(JWKS_PATH));
        document.put("end_session_endpoint",url(LOGOUT_PATH));
        server.stubFor(get(urlEqualTo(DISCOVERY_PATH)).willReturn(okJson(toJson(document))));
    }

    public void
    stubJwks(RSAKey key)
    {
        server.stubFor(
            get(urlEqualTo(JWKS_PATH))
                .willReturn(okJson(new JWKSet(key.toPublicJWK()).toJSONObject().toString())));
    }

    public void
    stubTokenReturning(String idToken)
    {
        server.stubFor(
            post(urlEqualTo(TOKEN_PATH))
                .willReturn(
                    okJson(
                        "{\"token_type\":\"Bearer\",\"expires_in\":3600,\"access_token\":\"okta-access\","
                            + "\"scope\":\"openid profile email groups\",\"id_token\":\"" + idToken + "\"}")));
    }

    public void
    stubTokenError()
    {
        server.stubFor(
            post(urlEqualTo(TOKEN_PATH))
                .willReturn(
                    aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type","application/json")
                        .withBody("{\"error\":\"invalid_grant\"}")));
    }

    public String
    idToken(String sub,String nonce)
    {
        return idToken(sub,nonce,builder -> {});
    }

    public String
    idToken(String sub,String nonce,Consumer<JWTClaimsSet.Builder> customizer)
    {
        return sign(signingKey,claims(sub,nonce,customizer));
    }

    public JWTClaimsSet
    claims(String sub,String nonce,Consumer<JWTClaimsSet.Builder> customizer)
    {
        Instant              now     = Instant.now();
        JWTClaimsSet.Builder builder =
            new JWTClaimsSet.Builder()
                .issuer(getIssuer())
                .audience(CLIENT_ID)
                .subject(sub)
                .claim("nonce",nonce)
                .claim("preferred_username",sub + "@example.com")
                .claim("email",sub + "@example.com")
                .claim("groups",List.of("Granite Admins","Everyone"))
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .jwtID(UUID.randomUUID().toString());

        customizer.accept(builder);
        return builder.build();
    }

    public String
    sign(RSAKey key,JWTClaimsSet claims)
    {
        try
        {
            SignedJWT signed =
                new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(),claims);

            signed.sign(new RSASSASigner(key.toRSAPrivateKey()));
            return signed.serialize();
        }
        catch (JOSEException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public RSAKey
    generate()
    {
        try
        {
            return new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString()).generate();
        }
        catch (JOSEException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public int
    discoveryRequests()
    {
        return server.findAll(getRequestedFor(urlEqualTo(DISCOVERY_PATH))).size();
    }

    public List<Map<String,String>>
    tokenRequests()
    {
        List<Map<String,String>> forms = new ArrayList<>();

        for (LoggedRequest request : server.findAll(postRequestedFor(urlEqualTo(TOKEN_PATH))))
            forms.add(parseForm(request.getBodyAsString()));

        return forms;
    }

    public List<String>
    tokenRequestContentTypes()
    {
        List<String> types = new ArrayList<>();

        for (LoggedRequest request : server.findAll(postRequestedFor(urlEqualTo(TOKEN_PATH))))
            types.add(request.getHeader("Content-Type"));

        return types;
    }

    public void
    resetRequests() { server.resetRequests(); }

    private Map<String,String>
    parseForm(String body)
    {
        Map<String,String> form = new LinkedHashMap<>();

        for (String pair : body.split("&"))
        {
            int separator = pair.indexOf('=');

            if (separator > 0)
                form.put(
                    URLDecoder.decode(pair.substring(0,separator),StandardCharsets.UTF_8),
                    URLDecoder.decode(pair.substring(separator + 1),StandardCharsets.UTF_8));
        }

        return form;
    }

    private String
    toJson(Map<String,Object> document)
    {
        StringBuilder json = new StringBuilder("{");

        for (Map.Entry<String,Object> entry : document.entrySet())
        {
            if (json.length() > 1)
                json.append(',');

            json.append('"').append(entry.getKey()).append("\":\"").append(entry.getValue()).append('"');
        }

        return json.append('}').toString();
    }
}

//////////////////////////////////////////////////////////////////////////////
