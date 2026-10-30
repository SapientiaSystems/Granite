//////////////////////////////////////////////////////////////////////////////
// OktaClient.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public
class OktaClient
    implements IOktaClient
{
    private static final String DISCOVERY_PATH      = "/.well-known/openid-configuration";
    private static final String ASSERTION_TYPE      = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
    private static final String NONCE_CLAIM         = "nonce";
    private static final String GROUPS_CLAIM        = "groups";
    private static final String USER_NAME_CLAIM     = "preferred_username";
    private static final String EMAIL_CLAIM         = "email";

    private final OktaSettings                           settings;
    private final OktaClientAssertionSigner              signer;
    private final Clock                                  clock;
    private final RestClient                             restClient;
    private final ConcurrentMap<String,NimbusJwtDecoder> decoders;
    private OktaEndpoints                                endpoints;
    private Instant                                      endpointsExpire;

    public
    OktaClient(OktaSettings settings,OktaClientAssertionSigner signer,Clock clock)
    {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(settings.getHttpTimeout());
        factory.setReadTimeout(settings.getHttpTimeout());

        this.settings        = Objects.requireNonNull(settings,"settings");
        this.signer          = signer;
        this.clock           = Objects.requireNonNull(clock,"clock");
        this.restClient      = RestClient.builder().requestFactory(factory).build();
        this.decoders        = new ConcurrentHashMap<>();
        this.endpoints       = null;
        this.endpointsExpire = null;
    }

    @Override
    public synchronized OktaEndpoints
    getEndpoints()
    {
        Instant now = clock.instant();

        if (endpoints != null && endpointsExpire.isAfter(now))
            return endpoints;

        endpoints       = discover();
        endpointsExpire = now.plus(settings.getDiscoveryCacheTtl());
        return endpoints;
    }

    @Override
    public String
    buildAuthorizeUrl(String state,String nonce,String codeChallenge,boolean promptLogin)
    {
        UriComponentsBuilder builder =
            UriComponentsBuilder
                .fromUriString(getEndpoints().getAuthorizationEndpoint())
                .queryParam("response_type","code")
                .queryParam("client_id",settings.getClientId())
                .queryParam("redirect_uri",settings.getRedirectUri())
                .queryParam("scope",settings.getScope())
                .queryParam("state",state)
                .queryParam("nonce",nonce)
                .queryParam("code_challenge",codeChallenge)
                .queryParam("code_challenge_method","S256");

        if (promptLogin)
            builder.queryParam("prompt","login");

        return builder.encode().build().toUriString();
    }

    @Override
    public OktaIdentity
    exchange(String code,String codeVerifier,String nonce)
    {
        OktaEndpoints current = getEndpoints();
        String        idToken = requestIdToken(current,code,codeVerifier);
        Jwt           jwt     = decode(current,idToken);

        verifyNonce(jwt,nonce);

        if (jwt.getExpiresAt() == null)
            throw new OktaLoginException("id token has no expiry");

        if (jwt.getSubject() == null || jwt.getSubject().isBlank())
            throw new OktaLoginException("id token has no subject");

        return
            new OktaIdentity(
                jwt.getSubject(),
                userNameOf(jwt),
                groupsOf(jwt));
    }

    private OktaEndpoints
    discover()
    {
        Map<String,Object> document;

        try
        {
            document =
                restClient
                    .get()
                    .uri(trimTrailingSlash(settings.getIssuer()) + DISCOVERY_PATH)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String,Object>>() {});
        }
        catch (RestClientException e)
        {
            throw new OktaLoginException("discovery request failed: " + e.getClass().getSimpleName());
        }

        if (document == null)
            throw new OktaLoginException("discovery document was empty");

        String issuer = stringOf(document,"issuer");

        if (!trimTrailingSlash(settings.getIssuer()).equals(trimTrailingSlash(issuer)))
            throw new OktaLoginException("discovery issuer does not match the configured issuer");

        OktaEndpoints discovered =
            new OktaEndpoints(
                issuer,
                stringOf(document,"authorization_endpoint"),
                stringOf(document,"token_endpoint"),
                stringOf(document,"jwks_uri"));

        if (discovered.getAuthorizationEndpoint() == null
            || discovered.getTokenEndpoint() == null
            || discovered.getJwksUri() == null)
            throw new OktaLoginException("discovery document is missing endpoints");

        return discovered;
    }

    private String
    requestIdToken(OktaEndpoints current,String code,String codeVerifier)
    {
        if (signer == null)
            throw new OktaLoginException("no client assertion key is configured");

        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();

        form.add("grant_type","authorization_code");
        form.add("code",code);
        form.add("redirect_uri",settings.getRedirectUri());
        form.add("code_verifier",codeVerifier);
        form.add("client_id",settings.getClientId());
        form.add("client_assertion_type",ASSERTION_TYPE);
        form.add("client_assertion",signer.sign(settings.getClientId(),current.getTokenEndpoint()));

        Map<String,Object> response;

        try
        {
            response =
                restClient
                    .post()
                    .uri(current.getTokenEndpoint())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String,Object>>() {});
        }
        catch (RestClientException e)
        {
            throw new OktaLoginException("token request failed: " + e.getClass().getSimpleName());
        }

        String idToken = response != null ? stringOf(response,"id_token") : null;

        if (idToken == null || idToken.isBlank())
            throw new OktaLoginException("token response has no id token");

        return idToken;
    }

    private Jwt
    decode(OktaEndpoints current,String idToken)
    {
        try
        {
            return decoders.computeIfAbsent(current.getJwksUri(),uri -> createDecoder(uri)).decode(idToken);
        }
        catch (JwtException e)
        {
            throw new OktaLoginException("id token rejected: " + e.getClass().getSimpleName());
        }
    }

    private NimbusJwtDecoder
    createDecoder(String jwksUri)
    {
        NimbusJwtDecoder decoder =
            NimbusJwtDecoder
                .withJwkSetUri(jwksUri)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        JwtTimestampValidator timestamps = new JwtTimestampValidator(settings.getClockSkew());

        timestamps.setClock(clock);
        decoder.setJwtValidator(
            new DelegatingOAuth2TokenValidator<>(
                timestamps,
                new JwtIssuerValidator(trimTrailingSlash(settings.getIssuer())),
                new JwtClaimValidator<Collection<String>>(
                    "aud",
                    audience -> audience != null && audience.contains(settings.getClientId()))));
        return decoder;
    }

    private void
    verifyNonce(Jwt jwt,String expected)
    {
        String actual = jwt.getClaimAsString(NONCE_CLAIM);

        if (expected == null || actual == null)
            throw new OktaLoginException("id token nonce is missing");

        if (!MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)))
            throw new OktaLoginException("id token nonce does not match");
    }

    private String
    userNameOf(Jwt jwt)
    {
        String preferred = jwt.getClaimAsString(USER_NAME_CLAIM);

        if (preferred != null && !preferred.isBlank())
            return preferred;

        String email = jwt.getClaimAsString(EMAIL_CLAIM);

        if (email != null && !email.isBlank())
            return email;

        return jwt.getSubject();
    }

    private Set<String>
    groupsOf(Jwt jwt)
    {
        Set<String>  groups = new LinkedHashSet<>();
        List<String> claim  = jwt.hasClaim(GROUPS_CLAIM) ? jwt.getClaimAsStringList(GROUPS_CLAIM) : null;

        if (claim != null)
            for (String group : claim)
                if (group != null && !group.isBlank())
                    groups.add(group);

        return groups;
    }

    private String
    stringOf(Map<String,Object> document,String name)
    {
        Object value = document.get(name);

        return value != null ? value.toString() : null;
    }

    private String
    trimTrailingSlash(String value)
    {
        if (value == null)
            return "";

        return value.endsWith("/") ? value.substring(0,value.length() - 1) : value;
    }
}

//////////////////////////////////////////////////////////////////////////////
