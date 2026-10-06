//////////////////////////////////////////////////////////////////////////////
// RouteConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import strata.foundation.spring.inject.ScopeConfiguration;

@Configuration
@Import({ScopeConfiguration.class})
public
class RouteConfiguration
{
    @Value("${granite.frontendcontroller-user-service.host}")
    private String frontendcontrollerUserServiceHost;

    @Value("${granite.frontendcontroller-admin-service.host}")
    private String frontendcontrollerAdminServiceHost;

    @Value("${granite.user-command-service.host}")
    private String userCommandServiceHost;

    @Value("${granite.user-query-service.host}")
    private String userQueryServiceHost;

    @Value("${granite.outbox-service.host}")
    private String outboxServiceHost;

    @Bean
    public RouteLocator
    routeLocator(RouteLocatorBuilder builder)
    {
        return
            builder
                .routes()
                .route(
                    "frontendcontroller-user-route",
                    route ->
                        route
                            .path("/frontendcontroller-user-service/**")
                            .uri(frontendcontrollerUserServiceHost))
                .route(
                    "frontendcontroller-admin-route",
                    route ->
                        route
                            .path("/frontendcontroller-admin-service/**")
                            .uri(frontendcontrollerAdminServiceHost))
                .route(
                    "user-command-route",
                    route ->
                        route
                            .path("/user-command-service/**")
                            .uri(userCommandServiceHost))
                .route(
                    "user-query-route",
                    route ->
                        route
                            .path("/user-query-service/**")
                            .uri(userQueryServiceHost))
                .route(
                    "outbox-route",
                    route ->
                        route
                            .path("/outbox-service/**")
                            .uri(outboxServiceHost))
                .build();
    }
}

//////////////////////////////////////////////////////////////////////////////
