////////////////////////////////////////////////////////////////////////////////
// ClientDtoToStringTest.java
////////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("CommitStage")
public
class ClientDtoToStringTest
{
    private static final String SECRET = "s3cr3t-value";

    @Test
    public void
    secretBearingDtosOmitSecretFromToString()
    {
        assertFalse(
            new CreateClientReply().setClientSecret(SECRET).toString().contains(SECRET));
        assertFalse(
            new RotateClientSecretReply().setClientSecret(SECRET).toString().contains(SECRET));
        assertFalse(
            new RotateOwnClientSecretReply().setClientSecret(SECRET).toString().contains(SECRET));
        assertFalse(
            new VerifyClientCredentialsRequest().setClientSecret(SECRET).toString().contains(SECRET));
    }
}

////////////////////////////////////////////////////////////////////////////////
