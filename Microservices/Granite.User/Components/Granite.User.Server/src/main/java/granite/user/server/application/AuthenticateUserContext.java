//////////////////////////////////////////////////////////////////////////////
// AuthenticateUserContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.AuthenticateUserRequest;
import granite.user.service.requestreply.CredentialKind;

import java.util.Optional;

public
class AuthenticateUserContext
{
    private AuthenticateUserRequest request;
    private Optional<User>          user;
    private boolean                 valid;
    private String                  failureMessage;

    public
    AuthenticateUserContext(AuthenticateUserRequest req)
    {
        request        = req;
        user           = Optional.empty();
        valid          = false;
        failureMessage = null;
    }

    public AuthenticateUserContext
    setUser(Optional<User> value)
    {
        user = value;
        return this;
    }

    public AuthenticateUserContext
    setValid(boolean value)
    {
        valid = value;
        return this;
    }

    public AuthenticateUserContext
    setFailureMessage(String value)
    {
        failureMessage = value;
        return this;
    }

    public AuthenticateUserRequest
    getRequest() { return request; }

    public String
    getUserName() { return request.getUserName(); }

    public String
    getCredential() { return request.getCredential(); }

    public CredentialKind
    getCredentialKind() { return request.getCredentialKind(); }

    public Optional<User>
    getUser() { return user; }

    public boolean
    getValid() { return valid; }

    public String
    getFailureMessage() { return failureMessage; }
}

//////////////////////////////////////////////////////////////////////////////
