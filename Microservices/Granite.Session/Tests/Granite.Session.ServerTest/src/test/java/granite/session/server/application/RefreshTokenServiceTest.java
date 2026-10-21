//////////////////////////////////////////////////////////////////////////////
// RefreshTokenServiceTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class RefreshTokenServiceTest
{
    private RefreshTokenService subject;

    @BeforeEach
    public void
    setUp()
    {
        subject = new RefreshTokenService();
    }

    @Test
    public void
    generateProduces256BitUrlSafeTokens()
    {
        String token = subject.generate();

        assertEquals(32,Base64.getUrlDecoder().decode(token).length);
        assertTrue(token.matches("[A-Za-z0-9_-]+"),"token should be base64url without padding: " + token);
    }

    @Test
    public void
    generateProducesDistinctTokens()
    {
        Set<String> tokens = new HashSet<>();

        for (int i = 0;i < 100;i++)
            tokens.add(subject.generate());

        assertEquals(100,tokens.size());
    }

    @Test
    public void
    hashIsDeterministicSha256Hex()
    {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            subject.hash("abc"));
        assertEquals(subject.hash("token"),subject.hash("token"));
        assertNotEquals(subject.hash("token-a"),subject.hash("token-b"));
    }

    @Test
    public void
    hashRejectsNull()
    {
        assertThrows(NullPointerException.class,() -> subject.hash(null));
    }
}

//////////////////////////////////////////////////////////////////////////////
