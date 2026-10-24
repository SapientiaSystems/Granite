//////////////////////////////////////////////////////////////////////////////
// OktaKeyLoader.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import granite.session.server.application.OktaClientAssertionSigner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;

public
class OktaKeyLoader
{
    private static final String PREFIX = "granite.session.okta.keystore.";

    private final Environment    environment;
    private final ResourceLoader loader;

    public
    OktaKeyLoader(Environment environment,ResourceLoader loader)
    {
        this.environment = environment;
        this.loader      = loader;
    }

    public OktaClientAssertionSigner
    load(Clock clock)
    {
        String   location    = environment.getRequiredProperty(PREFIX + "location");
        String   password    = environment.getProperty(PREFIX + "password","");
        String   type        = environment.getProperty(PREFIX + "type","PKCS12");
        String   alias       = environment.getRequiredProperty(PREFIX + "alias");
        String   keyPassword = environment.getProperty(PREFIX + "key-password",password);
        String   keyId       = environment.getProperty(PREFIX + "key-id","");
        Resource resource    = loader.getResource(location);

        if (resource instanceof ClassPathResource && !isClasspathKeyLoadingAllowed())
            throw
                new IllegalStateException(
                    PREFIX + "location=" + location +
                    " refers to a classpath resource, which is permitted only " +
                    "in the 'local' and 'development' environments; mount the key store externally");

        try (InputStream in = resource.getInputStream())
        {
            KeyStore store = KeyStore.getInstance(type);

            store.load(in,password.toCharArray());

            Key         key         = store.getKey(alias,keyPassword.toCharArray());
            Certificate certificate = store.getCertificate(alias);

            if (!(key instanceof RSAPrivateKey privateKey) || certificate == null)
                throw new IllegalStateException(PREFIX + "alias=" + alias + " is not an RSA private key entry");

            return
                new OktaClientAssertionSigner(
                    privateKey,
                    keyId.isBlank() ? thumbprint((RSAPublicKey)certificate.getPublicKey()) : keyId,
                    clock);
        }
        catch (IOException | GeneralSecurityException | JOSEException e)
        {
            throw new IllegalStateException("unable to load the Okta client key from " + location,e);
        }
    }

    private boolean
    isClasspathKeyLoadingAllowed()
    {
        return
            environment.acceptsProfiles(Profiles.of("local","development")) &&
            !environment.acceptsProfiles(Profiles.of("test","production"));
    }

    private String
    thumbprint(RSAPublicKey publicKey)
        throws JOSEException
    {
        return new RSAKey.Builder(publicKey).build().computeThumbprint().toString();
    }
}

//////////////////////////////////////////////////////////////////////////////
