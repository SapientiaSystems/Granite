//////////////////////////////////////////////////////////////////////////////
// JwkSetProvider.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public
class JwkSetProvider
    implements IJwkSetProvider
{
    private final JWKSet jwkSet;

    public
    JwkSetProvider(List<RSAPublicKey> publicKeys)
    {
        Objects.requireNonNull(publicKeys,"publicKeys");

        if (publicKeys.isEmpty())
            throw new IllegalArgumentException("publicKeys must not be empty");

        List<JWK> keys = new ArrayList<>();

        for (RSAPublicKey publicKey : publicKeys)
        {
            RSAKey key = toJwk(publicKey);

            if (keys.stream().noneMatch(existing -> existing.getKeyID().equals(key.getKeyID())))
                keys.add(key);
        }

        jwkSet = new JWKSet(keys);
    }

    @Override
    public Map<String,Object>
    getPublicJwkSet()
    {
        return new LinkedHashMap<>(jwkSet.toPublicJWKSet().toJSONObject());
    }

    @Override
    public List<String>
    getKeyIds()
    {
        List<String> ids = new ArrayList<>();

        for (JWK key : jwkSet.getKeys())
            ids.add(key.getKeyID());

        return ids;
    }

    private static RSAKey
    toJwk(RSAPublicKey publicKey)
    {
        try
        {
            return
                new RSAKey.Builder(publicKey)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyIDFromThumbprint()
                    .build();
        }
        catch (JOSEException e)
        {
            throw new IllegalStateException("failed to compute key id",e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
