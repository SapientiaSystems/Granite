//////////////////////////////////////////////////////////////////////////////
// GraniteJwtCheckTestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@EnableWebSecurity
public
class GraniteJwtCheckTestConfiguration
{
    @Bean
    public SecurityFilterChain
    securityFilterChain(
        HttpSecurity http,
        JwtDecoder   decoder,
        JwtCheckMode mode,
        Environment  environment)
        throws Exception
    {
        GraniteJwtCheckSecurity security = new GraniteJwtCheckSecurity();

        return
            security.jwtCheckChain(
                http,
                decoder,
                mode,
                security.defaultTokenlessPaths(environment));
    }

    @Bean
    public GraniteJwtCheckTestController
    controller()
    {
        return new GraniteJwtCheckTestController();
    }
}

//////////////////////////////////////////////////////////////////////////////
