//////////////////////////////////////////////////////////////////////////////
// ClientSecurityChainTestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.platform;

import granite.client.server.configuration.AbstractGraniteClientServerConfiguration;
import granite.client.service.requestreply.IClientCommandService;
import granite.client.service.requestreply.RotateOwnClientSecretReply;
import granite.client.service.requestreply.RotateOwnClientSecretRequest;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
@EnableWebMvc
@EnableWebSecurity
public
class ClientSecurityChainTestConfiguration
{
    public static final String                             SUBJECT = "client:gc_abcdefghijklmnopqrstuvwxyz234567";
    public static final AtomicReference<Optional<String>> SEEN    = new AtomicReference<>();

    @Bean
    public JwtDecoder
    jwtDecoder()
    {
        return
            token ->
            {
                if (!token.equals("good"))
                    throw new BadJwtException("bad token");

                return
                    Jwt
                        .withTokenValue(token)
                        .header("alg","RS256")
                        .subject(SUBJECT)
                        .issuedAt(Instant.now().minusSeconds(60))
                        .expiresAt(Instant.now().plusSeconds(600))
                        .claim("authorities",List.of("ROLE_USER"))
                        .build();
            };
    }

    @Bean
    public SecurityFilterChain
    securityFilterChain(HttpSecurity http,JwtDecoder decoder,Environment environment)
        throws Exception
    {
        return
            new AbstractGraniteClientServerConfiguration()
            {
                @Override
                protected String
                getServicePathPattern() { return "/client-command-service/**"; }
            }
                .securityFilterChain(http,decoder,environment);
    }

    @Bean
    public IClientCommandService
    service()
    {
        IClientCommandService service = Mockito.mock(IClientCommandService.class);

        Mockito
            .when(service.rotateOwnClientSecret(Mockito.any()))
            .thenAnswer(
                invocation ->
                {
                    SEEN.set(new JwtCallerSubject().get());
                    return
                        CompletableFuture.completedFuture(
                            new RotateOwnClientSecretReply(
                                (RotateOwnClientSecretRequest)invocation.getArgument(0)).setSuccess(true));
                });
        return service;
    }

    @Bean
    public ClientCommandServiceController
    controller(IClientCommandService service) { return new ClientCommandServiceController(service); }
}

//////////////////////////////////////////////////////////////////////////////
