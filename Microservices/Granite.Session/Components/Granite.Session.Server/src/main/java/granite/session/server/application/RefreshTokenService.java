//////////////////////////////////////////////////////////////////////////////
// RefreshTokenService.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;

public
class RefreshTokenService
    implements IRefreshTokenService
{
    private static final int TOKEN_BYTES = 32;

    private final SecureRandom random;

    public
    RefreshTokenService()
    {
        this(new SecureRandom());
    }

    public
    RefreshTokenService(SecureRandom random)
    {
        this.random = random;
    }

    @Override
    public String
    generate()
    {
        byte[] bytes = new byte[TOKEN_BYTES];

        random.nextBytes(bytes);
        return
            Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    @Override
    public String
    hash(String refreshToken)
    {
        Objects.requireNonNull(refreshToken,"refreshToken");

        try
        {
            return
                HexFormat
                    .of()
                    .formatHex(
                        MessageDigest
                            .getInstance("SHA-256")
                            .digest(refreshToken.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException(e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
