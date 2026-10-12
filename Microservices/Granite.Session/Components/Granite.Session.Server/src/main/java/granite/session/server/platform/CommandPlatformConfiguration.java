//////////////////////////////////////////////////////////////////////////////
// CommandPlatformConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import granite.session.server.application.IJwtTokenService;
import granite.session.server.application.IOauth2IdTokenValidator;
import granite.session.server.application.JwtTokenService;
import granite.session.server.application.Oauth2IdTokenValidator;
import granite.session.server.application.Oauth2ProvidersProperties;
import granite.session.service.event.ISessionEventSender;
import granite.session.service.requestreply.ISessionCommandService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.annotation.RequestScope;
import strata.outbox.core.repository.IOutboxEventRepository;
import strata.outbox.core.repository.RequestScopedRepositoryConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties(Oauth2ProvidersProperties.class)
@Import({RequestScopedRepositoryConfiguration.class})
public
class CommandPlatformConfiguration
    extends PlatformConfiguration
{
    private static final String AUDIENCE_PROPERTY = "granite.session.jwt.audience";

    @Bean
    @RequestScope
    public SessionCommandServiceController
    sessionCommandServiceController(ISessionCommandService service)
    {
        return new SessionCommandServiceController(service);
    }

    @Bean
    @RequestScope
    public ISessionEventSender
    sessionEventSender(IOutboxEventRepository repository)
    {
        return new OutboxEventSessionEventSender(repository);
    }

    @Bean
    @Scope("singleton")
    public IJwtTokenService
    jwtTokenService(
        Environment    environment,
        ResourceLoader loader,
        @Value("${granite.session.jwt.private-key-location:classpath:jwt-test-private-key.pem}")
        String         privateKeyLocation,
        @Value("${granite.session.jwt.public-key-location:classpath:jwt-test-public-key.pem}")
        String         publicKeyLocation,
        @Value("${granite.session.jwt.issuer:granite-session}")
        String         issuer,
        @Value("${granite.session.jwt.ttl:PT15M}")
        String         ttl)
    {
        boolean allowClasspath = isClasspathKeyLoadingAllowed(environment);

        Resource privateKeyResource = loader.getResource(privateKeyLocation);
        Resource publicKeyResource  = loader.getResource(publicKeyLocation);

        requireAllowed(allowClasspath,"private-key-location",privateKeyLocation,privateKeyResource);
        requireAllowed(allowClasspath,"public-key-location",publicKeyLocation,publicKeyResource);

        return
            new JwtTokenService(
                loadPrivateKey(privateKeyResource),
                loadPublicKey(publicKeyResource),
                issuer,
                readAudience(environment),
                Duration.parse(ttl));
    }

    @Bean
    @Scope("singleton")
    public IOauth2IdTokenValidator
    oauth2IdTokenValidator(Oauth2ProvidersProperties properties)
    {
        return new Oauth2IdTokenValidator(properties);
    }

    private boolean
    isClasspathKeyLoadingAllowed(Environment environment)
    {
        return
            environment.acceptsProfiles(Profiles.of("dev")) &&
            !environment.acceptsProfiles(Profiles.of("test","production"));
    }

    private void
    requireAllowed(boolean allowClasspath,String name,String location,Resource resource)
    {
        if (!allowClasspath && resource instanceof ClassPathResource)
            throw
                new IllegalStateException(
                    "granite.session.jwt." + name + "=" + location +
                    " refers to a classpath resource, which is permitted only " +
                    "in the 'dev' profile; mount the key externally " +
                    "(e.g. file:/mnt/secrets/jwt-private-key.pem)");
    }

    private List<String>
    readAudience(Environment environment)
    {
        return
            Binder
                .get(environment)
                .bind(AUDIENCE_PROPERTY,Bindable.listOf(String.class))
                .orElse(List.of("granite-services"));
    }

    private InputStream
    stripComments(InputStream in)
        throws IOException
    {
        StringBuilder builder = new StringBuilder();
        String        content = new String(in.readAllBytes(),StandardCharsets.UTF_8);

        for (String line : content.split("\\R"))
            if (!line.startsWith("#"))
                builder.append(line).append('\n');

        return
            new ByteArrayInputStream(
                builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    private RSAPrivateKey
    loadPrivateKey(Resource resource)
    {
        try (InputStream in = resource.getInputStream())
        {
            return RsaKeyConverters.pkcs8().convert(stripComments(in));
        }
        catch (IOException e)
        {
            throw
                new IllegalStateException(
                    "unable to read JWT private key: " + resource,e);
        }
    }

    private RSAPublicKey
    loadPublicKey(Resource resource)
    {
        try (InputStream in = resource.getInputStream())
        {
            return RsaKeyConverters.x509().convert(stripComments(in));
        }
        catch (IOException e)
        {
            throw
                new IllegalStateException(
                    "unable to read JWT public key: " + resource,e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
