//////////////////////////////////////////////////////////////////////////////
// PasswordPolicy.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import java.util.Optional;

public
class PasswordPolicy
{
    public static final int MAX_LENGTH = 128;

    private final int minLength;
    private final int maxLength;

    public
    PasswordPolicy(int minLength,int maxLength)
    {
        if (minLength < 1 || minLength > maxLength)
            throw new IllegalArgumentException("minLength must be between 1 and " + maxLength);

        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public int
    getMinLength() { return minLength; }

    public int
    getMaxLength() { return maxLength; }

    public Optional<String>
    check(String password)
    {
        if (password == null || password.isBlank() || password.length() < minLength)
            return Optional.of("password must be at least " + minLength + " characters");

        if (password.length() > maxLength)
            return Optional.of("password must be at most " + maxLength + " characters");

        return Optional.empty();
    }

    public static PasswordPolicy
    defaults()
    {
        return new PasswordPolicy(8,MAX_LENGTH);
    }
}

//////////////////////////////////////////////////////////////////////////////
