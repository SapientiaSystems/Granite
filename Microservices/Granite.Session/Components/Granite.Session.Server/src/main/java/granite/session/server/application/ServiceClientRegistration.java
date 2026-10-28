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
    public static final Duration MAX_LIFETIME = Duration.ofHours(1);

    private static final int    SHA256_LENGTH     = 32;
    private static final byte[] EMPTY_SECRET_HASH = hash("");

    private final String   clientId;
    private final byte[]   secretHash;
    private final Duration lifetime;

    public
    ServiceClientRegistration(String clientId,String secretSha256,Duration lifetime)
    {
        this.clientId   = Objects.requireNonNull(clientId,"clientId");
        this.secretHash = decode(secretSha256);
        this.lifetime   = Objects.requireNonNull(lifetime,"lifetime");

        if (MessageDigest.isEqual(secretHash,EMPTY_SECRET_HASH))
            throw new IllegalArgumentException("service client " + clientId + " is registered with the hash of an empty secret");

        if (lifetime.isZero() || lifetime.isNegative() || lifetime.compareTo(MAX_LIFETIME) > 0)
            throw new IllegalArgumentException("service client " + clientId + " lifetime must be positive and at most " + MAX_LIFETIME);
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
