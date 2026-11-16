//////////////////////////////////////////////////////////////////////////////
// PasswordPolicyTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class PasswordPolicyTest
{
    private final PasswordPolicy subject = new PasswordPolicy(8,128);

    @Test
    public void
    acceptsBoundaryLengths()
    {
        assertTrue(subject.check("x".repeat(8)).isEmpty());
        assertTrue(subject.check("x".repeat(128)).isEmpty());
    }

    @Test
    public void
    refusesTooShort()
    {
        assertEquals("password must be at least 8 characters",subject.check("x".repeat(7)).orElseThrow());
    }

    @Test
    public void
    refusesTooLong()
    {
        assertEquals("password must be at most 128 characters",subject.check("x".repeat(129)).orElseThrow());
    }

    @Test
    public void
    refusesNullAndBlank()
    {
        assertEquals("password must be at least 8 characters",subject.check(null).orElseThrow());
        assertEquals("password must be at least 8 characters",subject.check("         ").orElseThrow());
    }

    @Test
    public void
    refusesInvalidBounds()
    {
        assertThrows(IllegalArgumentException.class,() -> new PasswordPolicy(0,128));
        assertThrows(IllegalArgumentException.class,() -> new PasswordPolicy(129,128));
    }

    @Test
    public void
    seamReadsMinLengthAndRejectsOutOfRange()
    {
        org.springframework.mock.env.MockEnvironment environment = new org.springframework.mock.env.MockEnvironment();

        assertEquals(8,policy(environment).getMinLength());

        environment.setProperty("granite.user.password.min-length","12");
        assertEquals(12,policy(environment).getMinLength());

        environment.setProperty("granite.user.password.min-length","0");
        assertThrows(IllegalArgumentException.class,() -> policy(environment));

        environment.setProperty("granite.user.password.min-length","129");
        assertThrows(IllegalArgumentException.class,() -> policy(environment));
    }

    private PasswordPolicy
    policy(org.springframework.core.env.Environment environment)
    {
        return new granite.user.server.configuration.GraniteUserCommandServerConfiguration().passwordPolicy(environment);
    }
}

//////////////////////////////////////////////////////////////////////////////
