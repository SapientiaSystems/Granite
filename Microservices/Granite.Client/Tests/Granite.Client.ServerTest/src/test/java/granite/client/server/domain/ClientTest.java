//////////////////////////////////////////////////////////////////////////////
// ClientTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.domain;

import granite.client.server.application.ClientAuthorityPolicy;
import granite.client.server.application.ClientCredentials;
import granite.foundation.core.security.GraniteApplicationRolesManagerSupplier;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class ClientTest
{
    @Test
    public void
    secretIsCurrentOnlyBeforeExpiry()
    {
        Instant expiry = Instant.parse("2027-01-01T00:00:00Z");
        Client  client = new Client().setSecretExpiresAt(expiry);

        assertTrue(client.isSecretCurrent(expiry.minusMillis(1)));
        assertFalse(client.isSecretCurrent(expiry));
        assertTrue(new Client().isSecretCurrent(expiry));
    }

    @Test
    public void
    generatedIdentifiersHaveTheDocumentedShape()
    {
        assertTrue(ClientCredentials.newClientId().matches("gc_[a-z2-7]{32}"));
        assertEquals(43,ClientCredentials.newSecret().length());
        assertTrue(ClientCredentials.matches(ClientCredentials.hash("a"),"a"));
        assertFalse(ClientCredentials.matches(ClientCredentials.hash("a"),"b"));
    }

    @Test
    public void
    policyRejectsAdminScopeAndUnknownAndMintsOnlyCatalogRoles()
    {
        ClientAuthorityPolicy policy = new ClientAuthorityPolicy(new GraniteApplicationRolesManagerSupplier().get());

        assertThrows(IllegalArgumentException.class,() -> policy.validateAdded(Set.of(),Set.of("ROLE_ADMIN")));
        assertThrows(IllegalArgumentException.class,() -> policy.validateAdded(Set.of(),Set.of("SCOPE_internal")));
        assertThrows(IllegalArgumentException.class,() -> policy.validateAdded(Set.of(),Set.of("ROLE_NOPE")));
        assertEquals(Set.of("ROLE_USER"),policy.mintable(Set.of("ROLE_USER","ROLE_OLD","ROLE_ADMIN","SCOPE_internal")));
    }
}

//////////////////////////////////////////////////////////////////////////////
