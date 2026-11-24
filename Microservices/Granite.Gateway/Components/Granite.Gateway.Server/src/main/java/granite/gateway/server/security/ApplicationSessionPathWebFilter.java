//////////////////////////////////////////////////////////////////////////////
// ApplicationSessionPathWebFilter.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public
class ApplicationSessionPathWebFilter
    implements WebFilter
{
    public static final String       APPLICATION_CLAIM                  = "app";
    public static final String       CLIENT_ADMIN_APPLICATIONS_PROPERTY = "granite.client.admin-applications";
    public static final List<String> DEFAULT_CLIENT_ADMIN_APPLICATIONS  = List.of("client");

    private final Set<String>              applications;
    private final ServerWebExchangeMatcher allowed;

    public
    ApplicationSessionPathWebFilter(ServerWebExchangeMatcher allowed)
    {
        this(DEFAULT_CLIENT_ADMIN_APPLICATIONS,allowed);
    }

    public
    ApplicationSessionPathWebFilter(Collection<String> applications,ServerWebExchangeMatcher allowed)
    {
        Objects.requireNonNull(applications,"applications");

        Set<String> names = new LinkedHashSet<>();

        for (String application : applications)
            if (application != null && !application.isBlank())
                names.add(application.trim());

        this.applications = Set.copyOf(names);
        this.allowed      = Objects.requireNonNull(allowed,"allowed");
    }

    @Override
    public Mono<Void>
    filter(ServerWebExchange exchange,WebFilterChain chain)
    {
        return
            ReactiveSecurityContextHolder
                .getContext()
                .mapNotNull(context -> context.getAuthentication())
                .filter(authentication -> authentication.getPrincipal() instanceof Jwt)
                .map(authentication -> (Jwt)authentication.getPrincipal())
                .filter(jwt -> isConfined(jwt.getClaimAsString(APPLICATION_CLAIM)))
                .flatMap(jwt -> allowed.matches(exchange))
                .map(match -> !match.isMatch())
                .defaultIfEmpty(false)
                .flatMap(
                    refuse ->
                    {
                        if (!refuse)
                            return chain.filter(exchange);

                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    });
    }

    private boolean
    isConfined(String application)
    {
        return application != null && applications.contains(application);
    }
}

//////////////////////////////////////////////////////////////////////////////
