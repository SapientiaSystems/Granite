//////////////////////////////////////////////////////////////////////////////
// Oauth2ValidatedIdentity.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import java.util.Objects;
import java.util.Optional;

public
class Oauth2ValidatedIdentity
{
    private String           provider;
    private String           sub;
    private Optional<String> email;

    public
    Oauth2ValidatedIdentity()
    {
        provider = null;
        sub      = null;
        email    = Optional.empty();
    }

    public static Oauth2ValidatedIdentity
    of(String provider,String sub,Optional<String> email)
    {
        Objects.requireNonNull(provider,"provider");
        Objects.requireNonNull(sub,"sub");
        Objects.requireNonNull(email,"email");

        return
            new Oauth2ValidatedIdentity()
                .setProvider(provider)
                .setSub(sub)
                .setEmail(email);
    }

    public Oauth2ValidatedIdentity
    setProvider(String value)
    {
        provider = value;
        return this;
    }

    public String
    getProvider() { return provider; }

    public Oauth2ValidatedIdentity
    setSub(String value)
    {
        sub = value;
        return this;
    }

    public String
    getSub() { return sub; }

    public Oauth2ValidatedIdentity
    setEmail(Optional<String> value)
    {
        email = value == null ? Optional.empty() : value;
        return this;
    }

    public Optional<String>
    getEmail() { return email; }
}

//////////////////////////////////////////////////////////////////////////////
