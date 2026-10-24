//////////////////////////////////////////////////////////////////////////////
// OktaKeyLoaderTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import granite.session.server.application.OktaClientAssertionSigner;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.env.MockEnvironment;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class OktaKeyLoaderTest
{
    private static final String PASSWORD = "changeit-test";
    private static final String ALIAS    = "okta-client";

    @Test
    public void
    loadsTheKeyAndUsesTheThumbprintAsKeyId(@TempDir Path directory)
        throws Exception
    {
        Path                      store  = createKeyStore(directory);
        OktaClientAssertionSigner signer =
            new OktaKeyLoader(environment(store).withProperty("spring.profiles.active","test"),new DefaultResourceLoader())
                .load(Clock.systemUTC());
        RSAPublicKey              key    = publicKey(store);
        SignedJWT                 jwt    = SignedJWT.parse(signer.sign("client-1","https://okta.example.com/token"));

        assertEquals(new RSAKey.Builder(key).build().computeThumbprint().toString(),signer.getKeyId());
        assertEquals(signer.getKeyId(),jwt.getHeader().getKeyID());
        assertTrue(jwt.verify(new RSASSAVerifier(key)));
    }

    @Test
    public void
    configuredKeyIdWins(@TempDir Path directory)
        throws Exception
    {
        OktaClientAssertionSigner signer =
            new OktaKeyLoader(
                environment(createKeyStore(directory)).withProperty("granite.session.okta.keystore.key-id","kid-1"),
                new DefaultResourceLoader())
                .load(Clock.systemUTC());

        assertEquals("kid-1",signer.getKeyId());
    }

    @Test
    public void
    classpathKeyStoreIsRefusedOutsideLocalAndDevelopment()
    {
        MockEnvironment environment =
            new MockEnvironment()
                .withProperty("granite.session.okta.keystore.location","classpath:okta-client.p12")
                .withProperty("granite.session.okta.keystore.alias",ALIAS);

        environment.setActiveProfiles("production");

        assertThrows(
            IllegalStateException.class,
            () -> new OktaKeyLoader(environment,new DefaultResourceLoader()).load(Clock.systemUTC()));
    }

    @Test
    public void
    wrongAliasFails(@TempDir Path directory)
        throws Exception
    {
        MockEnvironment environment =
            environment(createKeyStore(directory)).withProperty("granite.session.okta.keystore.alias","missing");

        assertThrows(
            IllegalStateException.class,
            () -> new OktaKeyLoader(environment,new DefaultResourceLoader()).load(Clock.systemUTC()));
    }

    private MockEnvironment
    environment(Path store)
    {
        return
            new MockEnvironment()
                .withProperty("granite.session.okta.keystore.location",store.toUri().toString())
                .withProperty("granite.session.okta.keystore.password",PASSWORD)
                .withProperty("granite.session.okta.keystore.alias",ALIAS);
    }

    private Path
    createKeyStore(Path directory)
        throws Exception
    {
        Path    store   = directory.resolve("okta-client.p12");
        Process process =
            new ProcessBuilder(
                List.of(
                    Path.of(System.getProperty("java.home"),"bin","keytool").toString(),
                    "-genkeypair",
                    "-alias",ALIAS,
                    "-keyalg","RSA",
                    "-keysize","2048",
                    "-dname","CN=granite-okta-test",
                    "-validity","1",
                    "-storetype","PKCS12",
                    "-keystore",store.toString(),
                    "-storepass",PASSWORD,
                    "-keypass",PASSWORD))
                .redirectErrorStream(true)
                .start();

        process.getInputStream().readAllBytes();
        assertEquals(0,process.waitFor());
        return store;
    }

    private RSAPublicKey
    publicKey(Path store)
        throws Exception
    {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        try (InputStream in = Files.newInputStream(store))
        {
            keyStore.load(in,PASSWORD.toCharArray());
        }

        return (RSAPublicKey)keyStore.getCertificate(ALIAS).getPublicKey();
    }
}

//////////////////////////////////////////////////////////////////////////////
