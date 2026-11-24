//////////////////////////////////////////////////////////////////////////////
// ClientCredentials.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public
class ClientCredentials
{
    public static final String PREFIX = "gc_";

    private static final String       ALPHABET = "abcdefghijklmnopqrstuvwxyz234567";
    private static final SecureRandom RANDOM   = new SecureRandom();

    private
    ClientCredentials() {}

    public static String
    newClientId()
    {
        return newClientId(PREFIX);
    }

    public static String
    newClientId(String prefix)
    {
        byte[]        bytes  = new byte[20];
        StringBuilder result = new StringBuilder(prefix);

        RANDOM.nextBytes(bytes);

        int buffer = 0;
        int bits   = 0;

        for (byte b : bytes)
        {
            buffer = (buffer << 8) | (b & 0xFF);
            bits += 8;

            while (bits >= 5)
            {
                result.append(ALPHABET.charAt((buffer >> (bits - 5)) & 0x1F));
                bits -= 5;
            }

            buffer &= (1 << bits) - 1;
        }

        return result.toString();
    }

    public static String
    newSecret()
    {
        byte[] bytes = new byte[32];

        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
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
            throw new IllegalStateException("SHA-256 is not available",e);
        }
    }

    public static boolean
    matches(byte[] storedHash,String secret)
    {
        return
            storedHash != null
            && secret != null
            && MessageDigest.isEqual(storedHash,hash(secret));
    }
}

//////////////////////////////////////////////////////////////////////////////
