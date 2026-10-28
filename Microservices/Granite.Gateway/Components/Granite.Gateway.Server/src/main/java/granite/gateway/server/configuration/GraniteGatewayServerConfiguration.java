//////////////////////////////////////////////////////////////////////////////
// GraniteGatewayServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.GraniteAuthorities;
import granite.foundation.core.security.GraniteJwtAuthenticationConverter;
import granite.foundation.core.security.GraniteJwtDecoderFactory;
import granite.foundation.core.security.GraniteResourceServerSettings;
import granite.gateway.server.health.GatewayHealthCheckController;
import granite.gateway.server.security.CookieCsrfProtectionMatcher;
import granite.gateway.server.security.CsrfCookieWebFilter;
import granite.gateway.server.security.CurrentSessionFrontEndInputMatcher;
import granite.gateway.server.security.DeviceCookieWebFilter;
import granite.gateway.server.security.FixedWindowRateLimiter;
import granite.gateway.server.security.FrontEndInputKeyWebFilter;
import granite.gateway.server.security.ISessionResolver;
import granite.gateway.server.security.ISessionRevocationChecker;
import granite.gateway.server.security.PublicFrontEndInputMatcher;
import granite.gateway.server.security.RateLimitWebFilter;
import granite.gateway.server.security.RevocationCheckingReactiveJwtDecoder;
import granite.gateway.server.security.SessionCommandSessionResolver;
import granite.gateway.server.security.SessionCookieWebFilter;
import granite.gateway.server.security.SessionQueryRevocationChecker;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import strata.foundation.spring.inject.ScopeConfiguration;
import strata.foundation.spring.inject.SingletonScoped;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Import({ScopeConfiguration.class})
public
class GraniteGatewayServerConfiguration
{
    public static final String ISSUE_SERVICE_TOKEN_PATH = "/session-command-service/issue-service-token";

    @Bean
    public InitializingBean
    initialize()
    {
        return
            () ->
                SecurityContextHolder
                    .setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Bean
    public RouteLocator
    routeLocator(RouteLocatorBuilder builder,Environment environment)
    {
        return
            configureRoutes(builder.routes(),environment)
                .build();
    }

    @Bean
    public SecurityWebFilterChain
    securityWebFilterChain(
        ServerHttpSecurity                http,
        ReactiveJwtDecoder                jwtDecoder,
        GraniteJwtAuthenticationConverter jwtAuthenticationConverter,
        ISessionResolver                  sessionResolver,
        Environment                       environment)
    {
        ServerWebExchangeMatcher        publicMatcher  = getPublicExchangeMatcher(environment);
        CookieServerCsrfTokenRepository csrfRepository = createCsrfTokenRepository();

        return
            http
                .csrf(
                    csrf ->
                        csrf
                            .csrfTokenRepository(csrfRepository)
                            .csrfTokenRequestHandler(new ServerCsrfTokenRequestAttributeHandler())
                            .requireCsrfProtectionMatcher(new CookieCsrfProtectionMatcher()))
                .addFilterBefore(createServiceTokenRateLimitWebFilter(environment),SecurityWebFiltersOrder.CSRF)
                .addFilterBefore(
                    new FrontEndInputKeyWebFilter(
                        environment.getProperty("granite.gateway.front-end-input.max-bytes",Integer.class,1048576)),
                    SecurityWebFiltersOrder.CSRF)
                .addFilterBefore(new CsrfCookieWebFilter(csrfRepository),SecurityWebFiltersOrder.CSRF)
                .addFilterAfter(new DeviceCookieWebFilter(Duration.ofDays(365)),SecurityWebFiltersOrder.CSRF)
                .addFilterBefore(
                    new SessionCookieWebFilter(sessionResolver,publicMatcher),
                    SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(
                    exchanges ->
                    {
                        exchanges
                            .pathMatchers("/session-command-service/resolve-session")
                            .denyAll();
                        exchanges
                            .pathMatchers(
                                "/session-command-service/start-okta-login",
                                "/session-command-service/complete-okta-login")
                            .hasAuthority(GraniteAuthorities.SCOPE_INTERNAL);
                        exchanges.pathMatchers(HttpMethod.OPTIONS,"/**").permitAll();
                        exchanges.matchers(publicMatcher).permitAll();
                        exchanges.matchers(getAuthenticatedExchangeMatcher(environment)).authenticated();
                        configureAuthorization(exchanges,environment);
                        exchanges.anyExchange().authenticated();
                    })
                .oauth2ResourceServer(
                    server ->
                        server.jwt(
                            jwt ->
                                jwt
                                    .jwtDecoder(jwtDecoder)
                                    .jwtAuthenticationConverter(
                                        new ReactiveJwtAuthenticationConverterAdapter(
                                            jwtAuthenticationConverter))))
                .build();
    }

    @Bean
    @SingletonScoped
    public ReactiveJwtDecoder
    jwtDecoder(
        Environment                environment,
        ObjectProvider<SslBundles> sslBundles,
        ISessionRevocationChecker  revocationChecker)
    {
        return
            new RevocationCheckingReactiveJwtDecoder(
                createJwtDecoder(sslBundles,environment),
                revocationChecker);
    }

    @Bean
    @SingletonScoped
    public ISessionRevocationChecker
    revocationChecker(ObjectProvider<SslBundles> sslBundles,Environment environment)
    {
        if (!environment.getProperty("granite.gateway.revocation.enabled",Boolean.class,true))
            return jwt -> Mono.just(true);

        URI host = URI.create(environment.getRequiredProperty("granite.session-query-service.host"));

        return
            new SessionQueryRevocationChecker(
                createRevocationWebClient(sslBundles,environment),
                UriComponentsBuilder
                    .newInstance()
                    .scheme(host.getScheme())
                    .host(host.getHost())
                    .port(host.getPort())
                    .path("/session-query-service/find-sessions-by-key")
                    .build()
                    .toUri(),
                Duration.parse(environment.getProperty("granite.gateway.revocation.cache-ttl","PT30S")),
                Duration.parse(environment.getProperty("granite.gateway.revocation.timeout","PT2S")),
                Clock.systemUTC());
    }

    @Bean
    @SingletonScoped
    public ISessionResolver
    sessionResolver(
        ObjectProvider<SslBundles> sslBundles,
        Environment                environment)
    {
        return
            new SessionCommandSessionResolver(
                createSessionResolveWebClient(sslBundles,environment),
                URI.create(
                    environment.getRequiredProperty("granite.session-command-service.host")
                        + "/session-command-service/resolve-session"),
                Duration.parse(environment.getProperty("granite.gateway.session-cache-ttl","PT60S")),
                Duration.parse(environment.getProperty("granite.gateway.session-resolve.timeout","PT2S")),
                Clock.systemUTC());
    }

    @Bean
    @SingletonScoped
    public GraniteJwtAuthenticationConverter
    jwtAuthenticationConverter()
    {
        return new GraniteJwtAuthenticationConverter();
    }

    @Bean
    @SingletonScoped
    public GatewayHealthCheckController
    healthCheckController()
    {
        return new GatewayHealthCheckController();
    }

    protected ReactiveJwtDecoder
    createJwtDecoder(ObjectProvider<SslBundles> sslBundles,Environment environment)
    {
        GraniteResourceServerSettings settings = GraniteResourceServerSettings.from(environment);
        NimbusReactiveJwtDecoder      decoder  =
            NimbusReactiveJwtDecoder
                .withJwkSetUri(
                    settings
                        .getJwkSetUri()
                        .orElseThrow(
                            () ->
                                new IllegalStateException(
                                    GraniteResourceServerSettings.JWK_SET_URI + " must be configured")))
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .webClient(
                    createTrustingWebClient(
                        sslBundles,
                        environment.getProperty(
                            GraniteResourceServerSettings.SSL_BUNDLE,
                            environment.getProperty("spring.cloud.gateway.httpclient.ssl.bundle",""))))
                .build();

        decoder.setJwtValidator(new GraniteJwtDecoderFactory().createValidator(settings));
        return decoder;
    }

    protected WebClient
    createRevocationWebClient(ObjectProvider<SslBundles> sslBundles,Environment environment)
    {
        return
            createTrustingWebClient(
                sslBundles,
                environment.getProperty(
                    "granite.gateway.revocation.ssl-bundle",
                    environment.getProperty("spring.cloud.gateway.httpclient.ssl.bundle","")));
    }

    protected WebClient
    createSessionResolveWebClient(ObjectProvider<SslBundles> sslBundles,Environment environment)
    {
        return
            createTrustingWebClient(
                sslBundles,
                environment.getProperty(
                    "granite.gateway.session-resolve.ssl-bundle",
                    environment.getProperty("spring.cloud.gateway.httpclient.ssl.bundle","")));
    }

    protected WebClient
    createTrustingWebClient(ObjectProvider<SslBundles> sslBundles,String bundle)
    {
        if (bundle.isBlank())
            return WebClient.builder().build();

        try
        {
            SslContext context =
                SslContextBuilder
                    .forClient()
                    .trustManager(
                        sslBundles
                            .getObject()
                            .getBundle(bundle)
                            .getManagers()
                            .getTrustManagerFactory())
                    .build();

            return
                WebClient
                    .builder()
                    .clientConnector(
                        new ReactorClientHttpConnector(
                            HttpClient.create().secure(spec -> spec.sslContext(context))))
                    .build();
        }
        catch (SSLException e)
        {
            throw new IllegalStateException("unable to configure TLS for session revocation lookups",e);
        }
    }

    protected List<String>
    getPublicPathPatterns(Environment environment)
    {
        List<String> paths =
            new ArrayList<>(
                List.of(
                    environment.getProperty("strata.health.base-path","/health") + "/**",
                    ISSUE_SERVICE_TOKEN_PATH,
                    "/",
                    "/index.html",
                    "/config.js",
                    "/favicon.ico",
                    "/assets/**"));

        if (!environment.acceptsProfiles(Profiles.of("production")))
            paths.addAll(
                List.of(
                    "/session-command-service/login",
                    "/session-command-service/refresh",
                    "/*/swagger-ui/**",
                    "/*/swagger-ui.html",
                    "/*/v3/api-docs/**"));

        return paths;
    }

    protected RateLimitWebFilter
    createServiceTokenRateLimitWebFilter(Environment environment)
    {
        return
            new RateLimitWebFilter(
                ServerWebExchangeMatchers.pathMatchers(ISSUE_SERVICE_TOKEN_PATH),
                new FixedWindowRateLimiter(
                    environment.getProperty("granite.gateway.issue-service-token.rate-limit.requests",Integer.class,10),
                    Duration.parse(
                        environment.getProperty("granite.gateway.issue-service-token.rate-limit.window","PT1M")),
                    environment.getProperty(
                        "granite.gateway.issue-service-token.rate-limit.max-clients",
                        Integer.class,
                        10000),
                    Clock.systemUTC()));
    }

    protected CookieServerCsrfTokenRepository
    createCsrfTokenRepository()
    {
        CookieServerCsrfTokenRepository repository = CookieServerCsrfTokenRepository.withHttpOnlyFalse();

        repository.setCookiePath("/");
        repository.setCookieCustomizer(cookie -> cookie.httpOnly(false).sameSite("Strict"));
        return repository;
    }

    protected ServerWebExchangeMatcher
    getPublicExchangeMatcher(Environment environment)
    {
        return
            new OrServerWebExchangeMatcher(
                ServerWebExchangeMatchers.pathMatchers(getPublicPathPatterns(environment).toArray(String[]::new)),
                new PublicFrontEndInputMatcher());
    }

    protected ServerWebExchangeMatcher
    getAuthenticatedExchangeMatcher(Environment environment)
    {
        return new CurrentSessionFrontEndInputMatcher();
    }

    protected final ServerHttpSecurity.AuthorizeExchangeSpec
    configureAuthorization(AuthorizeExchangeSpec exchanges,Environment environment)
    {
        String[] readers = getUsersReaderAuthorities(environment).toArray(String[]::new);
        String[] admins  = {ApplicationRolesManager.ADMIN,GraniteAuthorities.SCOPE_INTERNAL};

        if (environment.acceptsProfiles(Profiles.of("production")))
            exchanges
                .pathMatchers(
                    "/user-query-service/**",
                    "/user-command-service/**",
                    "/session-query-service/**",
                    "/session-command-service/**",
                    "/outbox-service/**")
                    .hasAuthority(GraniteAuthorities.SCOPE_INTERNAL);

        exchanges
            .pathMatchers(
                HttpMethod.POST,
                "/frontendcontroller-user-service/get-preferences-output",
                "/frontendcontroller-admin-service/get-preferences-output",
                "/frontendcontroller-user-service/handle-preferences-input",
                "/frontendcontroller-admin-service/handle-preferences-input")
                .authenticated()
            .pathMatchers(
                HttpMethod.POST,
                "/frontendcontroller-user-service/get-users-output",
                "/frontendcontroller-admin-service/get-users-output")
                .hasAnyAuthority(readers)
            .pathMatchers("/user-query-service/**")
                .hasAnyAuthority(readers)
            .pathMatchers(HttpMethod.POST,"/session-command-service/logout")
                .hasAnyAuthority(readers)
            .pathMatchers("/session-query-service/**")
                .hasAnyAuthority(readers)
            .pathMatchers(
                HttpMethod.POST,
                "/frontendcontroller-user-service/handle-users-input",
                "/frontendcontroller-admin-service/handle-users-input")
                .hasAnyAuthority(admins)
            .pathMatchers(
                "/frontendcontroller-user-service/handle-input",
                "/frontendcontroller-admin-service/handle-input",
                "/frontendcontroller-user-service/get-output",
                "/frontendcontroller-admin-service/get-output",
                "/user-command-service/**",
                "/session-command-service/**",
                "/outbox-service/**")
                .hasAnyAuthority(admins);

        configureApplicationAuthorization(exchanges,environment);

        return
            exchanges
                .pathMatchers("/**")
                    .hasAnyAuthority(admins);
    }

    protected List<String>
    getUsersReaderAuthorities(Environment environment)
    {
        return List.of(ApplicationRolesManager.ADMIN,GraniteAuthorities.SCOPE_INTERNAL);
    }

    protected ServerHttpSecurity.AuthorizeExchangeSpec
    configureApplicationAuthorization(AuthorizeExchangeSpec exchanges,Environment environment)
    {
        return exchanges;
    }

    protected Builder
    configureRoutes(Builder routes,Environment environment)
    {
        addRoute(
            routes,
            "frontendcontroller-user-route",
            "/frontendcontroller-user-service/**",
            "granite.frontendcontroller-user-service.host",
            environment);
        addRoute(
            routes,
            "frontendcontroller-admin-route",
            "/frontendcontroller-admin-service/**",
            "granite.frontendcontroller-admin-service.host",
            environment);

        addRoute(
            routes,
            "user-command-route",
            "/user-command-service/**",
            "granite.user-command-service.host",
            environment);
        addRoute(
            routes,
            "user-query-route",
            "/user-query-service/**",
            "granite.user-query-service.host",
            environment);
        addRoute(
            routes,
            "session-command-route",
            "/session-command-service/**",
            "granite.session-command-service.host",
            environment);
        addRoute(
            routes,
            "session-query-route",
            "/session-query-service/**",
            "granite.session-query-service.host",
            environment);
        addRoute(
            routes,
            "outbox-route",
            "/outbox-service/**",
            "granite.outbox-service.host",
            environment);
        return configureApplicationRoutes(routes,environment);
    }

    protected Builder
    configureApplicationRoutes(Builder routes,Environment environment)
    {
        return routes;
    }

    protected Builder
    addRoute(
        Builder     routes,
        String      id,
        String      pathPattern,
        String      hostProperty,
        Environment environment)
    {
        String host = environment.getRequiredProperty(hostProperty);

        return
            routes.route(
                id,
                route ->
                    route
                        .path(pathPattern)
                        .uri(host));
    }
}

//////////////////////////////////////////////////////////////////////////////
