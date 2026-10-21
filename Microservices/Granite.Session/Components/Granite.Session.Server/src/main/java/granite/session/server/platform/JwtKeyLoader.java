//////////////////////////////////////////////////////////////////////////////
// JwtKeyLoader.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.converter.RsaKeyConverters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public
class JwtKeyLoader
{
    private final Environment    environment;
    private final ResourceLoader loader;

    public
    JwtKeyLoader(Environment environment,ResourceLoader loader)
    {
        this.environment = environment;
        this.loader      = loader;
    }

    public KeyPair
    load(String privateKeyLocation,String publicKeyLocation)
    {
        boolean  allowClasspath     = isClasspathKeyLoadingAllowed();
        Resource privateKeyResource = loader.getResource(privateKeyLocation);
        Resource publicKeyResource  = loader.getResource(publicKeyLocation);

        requireAllowed(allowClasspath,"private-key-location",privateKeyLocation,privateKeyResource);
        requireAllowed(allowClasspath,"public-key-location",publicKeyLocation,publicKeyResource);

        RSAPrivateKey privateKey = loadPrivateKey(privateKeyResource);
        RSAPublicKey  publicKey  = loadPublicKey(publicKeyResource);

        return new KeyPair(publicKey,privateKey);
    }

    public RSAPublicKey
    loadPublic(String name,String publicKeyLocation)
    {
        Resource publicKeyResource = loader.getResource(publicKeyLocation);

        requireAllowed(isClasspathKeyLoadingAllowed(),name,publicKeyLocation,publicKeyResource);
        return loadPublicKey(publicKeyResource);
    }

    private boolean
    isClasspathKeyLoadingAllowed()
    {
        return
            environment.acceptsProfiles(Profiles.of("local","development")) &&
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
                    "in the 'local' and 'development' environments; mount the key externally " +
                    "(e.g. file:/mnt/secrets/jwt-private-key.pem)");
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
