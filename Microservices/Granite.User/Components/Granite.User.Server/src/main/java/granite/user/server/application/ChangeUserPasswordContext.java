//////////////////////////////////////////////////////////////////////////////
// ChangeUserPasswordContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.ChangeUserPasswordRequest;

import java.util.Optional;

public
class ChangeUserPasswordContext
{
    private ChangeUserPasswordRequest request;
    private Optional<User>            user;
    private boolean                   valid;
    private boolean                   locked;
    private boolean                   changed;
    private String                    failureMessage;

    public
    ChangeUserPasswordContext(ChangeUserPasswordRequest req)
    {
        request        = req;
        user           = Optional.empty();
        valid          = false;
        locked         = false;
        changed        = false;
        failureMessage = null;
    }

    public ChangeUserPasswordContext
    setUser(Optional<User> value)
    {
        user = value;
        return this;
    }

    public ChangeUserPasswordContext
    setValid(boolean value)
    {
        valid = value;
        return this;
    }

    public ChangeUserPasswordContext
    setLocked(boolean value)
    {
        locked = value;
        return this;
    }

    public ChangeUserPasswordContext
    setChanged(boolean value)
    {
        changed = value;
        return this;
    }

    public ChangeUserPasswordContext
    setFailureMessage(String value)
    {
        failureMessage = value;
        return this;
    }

    public ChangeUserPasswordRequest
    getRequest() { return request; }

    public Long
    getUserId() { return request.getUserId(); }

    public String
    getCurrentPassword() { return request.getCurrentPassword(); }

    public String
    getNewPassword() { return request.getNewPassword(); }

    public Optional<User>
    getUser() { return user; }

    public boolean
    getValid() { return valid; }

    public boolean
    isLocked() { return locked; }

    public boolean
    isChanged() { return changed; }

    public String
    getFailureMessage() { return failureMessage; }
}

//////////////////////////////////////////////////////////////////////////////
