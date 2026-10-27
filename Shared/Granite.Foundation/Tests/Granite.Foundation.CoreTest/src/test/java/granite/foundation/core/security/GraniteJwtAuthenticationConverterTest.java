//////////////////////////////////////////////////////////////////////////////
// GraniteJwtAuthenticationConverterTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.security;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class GraniteJwtAuthenticationConverterTest
{
    @Test
    public void
    principalNameIsSubject()
    {
        AbstractAuthenticationToken token =
            new GraniteJwtAuthenticationConverter().convert(jwt("authorities",List.of("ROLE_USER")));

        assertEquals("1001",token.getName());
        assertTrue(token.isAuthenticated());
    }

    @Test
    public void
    authoritiesClaimBecomesGrantedAuthorities()
    {
        AbstractAuthenticationToken token =
            new GraniteJwtAuthenticationConverter()
                .convert(jwt("authorities",List.of("ROLE_ADMIN","SCOPE_internal")));

        assertEquals(Set.of("ROLE_ADMIN","SCOPE_internal"),names(token));
    }

    @Test
    public void
    missingOrMalformedClaimYieldsNoAuthorities()
    {
        GraniteJwtAuthenticationConverter subject = new GraniteJwtAuthenticationConverter();

        assertTrue(names(subject.convert(jwt("other",List.of("ROLE_ADMIN")))).isEmpty());
        assertTrue(names(subject.convert(jwt("authorities","ROLE_ADMIN"))).isEmpty());
        assertEquals(Set.of("ROLE_USER"),names(subject.convert(jwt("authorities",List.of("ROLE_USER","",7)))));
    }

    @Test
    public void
    customClaimNameIsHonoured()
    {
        AbstractAuthenticationToken token =
            new GraniteJwtAuthenticationConverter("roles").convert(jwt("roles",List.of("ROLE_AUDITOR")));

        assertEquals(Set.of("ROLE_AUDITOR"),names(token));
    }

    private static Jwt
    jwt(String claim,Object value)
    {
        return
            Jwt
                .withTokenValue("token")
                .header("alg","RS256")
                .subject("1001")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim(claim,value)
                .build();
    }

    private static Set<String>
    names(AbstractAuthenticationToken token)
    {
        return
            token
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}

//////////////////////////////////////////////////////////////////////////////
