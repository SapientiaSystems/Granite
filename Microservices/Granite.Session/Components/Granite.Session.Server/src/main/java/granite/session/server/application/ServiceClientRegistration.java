//////////////////////////////////////////////////////////////////////////////
// ServiceClientRegistration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;

public
class ServiceClientRegistration
{
    private static final int SHA256_LENGTH = 32;

    private final String   clientId;
    private final byte[]   secretHash;
    private final Duration lifetime;

    public
    ServiceClientRegistration(String clientId,String secretSha256,Duration lifetime)
    {
        this.clientId   = Objects.requireNonNull(clientId,"clientId");
        this.secretHash = decode(secretSha256);
        this.lifetime   = Objects.requireNonNull(lifetime,"lifetime");

        if (lifetime.isZero() || lifetime.isNegative())
            throw new IllegalArgumentException("lifetime must be positive");
    }

    public String
    getClientId() { return clientId; }

    public Duration
    getLifetime() { return lifetime; }

    public boolean
    matches(String secret)
    {
        return MessageDigest.isEqual(secretHash,hash(secret != null ? secret : ""));
    }

    public static byte[]
    hash(String secret)
    {
        try
        {
            return
                MessageDigest
                    .getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static byte[]
    decode(String secretSha256)
    {
        String value = secretSha256 != null ? secretSha256.trim() : "";
        byte[] decoded;

        if (value.length() == SHA256_LENGTH * 2 && value.matches("[0-9a-fA-F]+"))
            decoded = HexFormat.of().parseHex(value);
        else
            decoded = decodeBase64(value);

        if (decoded.length != SHA256_LENGTH)
            throw new IllegalArgumentException("secret hash must be a hex or base64 SHA-256 value");

        return decoded;
    }

    private static byte[]
    decodeBase64(String value)
    {
        try
        {
            return Base64.getDecoder().decode(value);
        }
        catch (IllegalArgumentException e)
        {
            try
            {
                return Base64.getUrlDecoder().decode(value);
            }
            catch (IllegalArgumentException ignored)
            {
                return new byte[0];
            }
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
