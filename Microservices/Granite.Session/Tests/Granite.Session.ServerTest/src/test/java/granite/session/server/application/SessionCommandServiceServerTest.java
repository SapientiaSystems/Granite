//////////////////////////////////////////////////////////////////////////////
// SessionCommandServiceServerTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import granite.session.service.requestreply.ISessionCommandService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Tag("CommitStage")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
@ActiveProfiles("dev")
public
class SessionCommandServiceServerTest
    extends granite.session.service.requestreply.SessionCommandServiceTest
{
    @Autowired
    private ISessionCommandService subject;

    @Autowired
    private RSAKey signingKey;

    @Value("${granite.session.oauth2.providers.test.issuer-uri}")
    private String issuer;

    @Value("${granite.session.oauth2.providers.test.audience}")
    private String audience;

    @BeforeEach
    public void
    setUp()
    {
        RequestContextHolder.setRequestAttributes(new RequestAttributeMap());
    }

    @AfterEach
    public void
    tearDown()
    {
        RequestContextHolder.resetRequestAttributes();
    }

    @Override
    protected ISessionCommandService
    getSubject() { return subject; }

    @Override
    protected String
    oauth2IdToken()
    {
        Instant      now    = Instant.now();
        JWTClaimsSet claims =
            new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject("external-" + UUID.randomUUID())
                .claim("email","external@example.com")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .build();

        try
        {
            SignedJWT signed =
                new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(signingKey.getKeyID())
                        .build(),
                    claims);

            signed.sign(new RSASSASigner(signingKey.toRSAPrivateKey()));
            return signed.serialize();
        }
        catch (JOSEException e)
        {
            throw new IllegalStateException("failed to sign test id token",e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
