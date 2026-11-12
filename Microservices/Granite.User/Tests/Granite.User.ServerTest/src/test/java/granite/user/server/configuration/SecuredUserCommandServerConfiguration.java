//////////////////////////////////////////////////////////////////////////////
// SecuredUserCommandServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.configuration;

import granite.foundation.core.security.GraniteJwtDecoderFactory;
import granite.foundation.core.security.GraniteResourceServerSettings;
import granite.user.server.application.TestConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import strata.foundation.spring.inject.SingletonScoped;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Configuration
public
class SecuredUserCommandServerConfiguration
    extends TestConfiguration
{
    private final KeyPair signingKeys;

    public
    SecuredUserCommandServerConfiguration()
        throws NoSuchAlgorithmException
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);
        signingKeys = generator.generateKeyPair();
    }

    @Bean
    @SingletonScoped
    public KeyPair
    signingKeys()
    {
        return signingKeys;
    }

    @Override
    @Bean
    @SingletonScoped
    public JwtDecoder
    jwtDecoder(Environment environment,ObjectProvider<SslBundles> sslBundles)
    {
        NimbusJwtDecoder decoder =
            NimbusJwtDecoder
                .withPublicKey((RSAPublicKey)signingKeys.getPublic())
                .build();

        decoder.setJwtValidator(
            new GraniteJwtDecoderFactory()
                .createValidator(
                    new GraniteResourceServerSettings(
                        null,
                        GraniteResourceServerSettings.DEFAULT_ISSUER,
                        List.of(GraniteResourceServerSettings.DEFAULT_AUDIENCE))));
        return decoder;
    }
}

//////////////////////////////////////////////////////////////////////////////
