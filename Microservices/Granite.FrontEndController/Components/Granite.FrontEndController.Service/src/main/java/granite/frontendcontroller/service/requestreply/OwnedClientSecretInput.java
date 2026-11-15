//////////////////////////////////////////////////////////////////////////////
// OwnedClientSecretInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndInput;

public
class OwnedClientSecretInput
    extends FrontEndInput
{
    private String clientId;

    public
    OwnedClientSecretInput()
    {
        super();
        clientId = null;
    }

    @Override
    public OwnedClientSecretInput
    setUserId(Long id)
    {
        return (OwnedClientSecretInput)super.setUserId(id);
    }

    @Override
    public OwnedClientSecretInput
    setSource(String src)
    {
        return (OwnedClientSecretInput)super.setSource(src);
    }

    @Override
    public OwnedClientSecretInput
    setDestination(String dest)
    {
        return (OwnedClientSecretInput)super.setDestination(dest);
    }

    @Override
    public OwnedClientSecretInput
    setCommand(String cmd)
    {
        return (OwnedClientSecretInput)super.setCommand(cmd);
    }

    public OwnedClientSecretInput
    setClientId(String id)
    {
        clientId = id;
        return this;
    }

    public String
    getClientId() { return clientId; }
}

//////////////////////////////////////////////////////////////////////////////
