//////////////////////////////////////////////////////////////////////////////
// GraniteJwtCheckSecurity.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

public
class GraniteJwtCheckSecurity
{
    public SecurityFilterChain
    jwtCheckChain(
        HttpSecurity http,
        JwtDecoder   decoder,
        JwtCheckMode mode,
        List<String> tokenlessPaths)
        throws Exception
    {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(
                requests ->
                {
                    requests.requestMatchers(antMatcher(HttpMethod.OPTIONS,"/**")).permitAll();

                    for (String path : tokenlessPaths)
                        requests.requestMatchers(antMatcher(path)).permitAll();

                    if (mode == JwtCheckMode.REQUIRED)
                        requests.anyRequest().authenticated();
                    else
                        requests.anyRequest().permitAll();
                })
            .oauth2ResourceServer(
                server ->
                    server.jwt(
                        jwt ->
                            jwt
                                .decoder(decoder)
                                .jwtAuthenticationConverter(new GraniteJwtAuthenticationConverter())));
        return http.build();
    }

    public List<String>
    defaultTokenlessPaths(Environment environment)
    {
        List<String> paths =
            new ArrayList<>(
                List.of(
                    environment.getProperty("strata.health.base-path","/health") + "/**",
                    "/error"));

        if (!environment.acceptsProfiles(Profiles.of("production")))
            paths.addAll(
                List.of(
                    environment.getProperty("springdoc.api-docs.path","/v3/api-docs") + "/**",
                    environment.getProperty("springdoc.swagger-ui.path","/swagger-ui") + "/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"));

        return paths;
    }
}

//////////////////////////////////////////////////////////////////////////////
