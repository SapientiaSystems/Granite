//////////////////////////////////////////////////////////////////////////////
// GraniteOutboxSecurityConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.configuration;

import granite.foundation.core.security.GraniteJwtCheckSecurity;
import granite.foundation.core.security.GraniteJwtDecoderFactory;
import granite.foundation.core.security.GraniteResourceServerSettings;
import granite.foundation.core.security.JwtCheckMode;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
@EnableWebSecurity
public
class GraniteOutboxSecurityConfiguration
{
    @Bean
    public SecurityFilterChain
    securityFilterChain(
        HttpSecurity http,
        JwtDecoder   jwtDecoder,
        Environment  environment)
        throws Exception
    {
        return
            new GraniteJwtCheckSecurity()
                .jwtCheckChain(
                    http,
                    jwtDecoder,
                    JwtCheckMode.REQUIRED,
                    getTokenlessPathPatterns(environment));
    }

    @Bean
    @Scope("singleton")
    public JwtDecoder
    jwtDecoder(Environment environment,ObjectProvider<SslBundles> sslBundles)
    {
        return
            new GraniteJwtDecoderFactory()
                .create(
                    GraniteResourceServerSettings.from(environment),
                    createJwkSetRestOperations(sslBundles,environment));
    }

    protected RestOperations
    createJwkSetRestOperations(ObjectProvider<SslBundles> sslBundles,Environment environment)
    {
        String bundle = environment.getProperty(GraniteResourceServerSettings.SSL_BUNDLE,"");

        if (bundle.isBlank())
            return new RestTemplate();

        return
            new RestTemplateBuilder()
                .sslBundle(sslBundles.getObject().getBundle(bundle))
                .build();
    }

    protected List<String>
    getTokenlessPathPatterns(Environment environment)
    {
        return new GraniteJwtCheckSecurity().defaultTokenlessPaths(environment);
    }
}

//////////////////////////////////////////////////////////////////////////////
