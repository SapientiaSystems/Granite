//////////////////////////////////////////////////////////////////////////////
// RedirectOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndOutput;

public
class RedirectOutput
    extends FrontEndOutput
{
    private String url;

    public
    RedirectOutput()
    {
        super();
        url = null;
    }

    @Override
    public RedirectOutput
    setUserId(Long id)
    {
        return (RedirectOutput)super.setUserId(id);
    }

    @Override
    public RedirectOutput
    setSource(String src)
    {
        return (RedirectOutput)super.setSource(src);
    }

    @Override
    public RedirectOutput
    setDestination(String dest)
    {
        return (RedirectOutput)super.setDestination(dest);
    }

    @Override
    public RedirectOutput
    setCommand(String cmd)
    {
        return (RedirectOutput)super.setCommand(cmd);
    }

    public RedirectOutput
    setUrl(String value)
    {
        url = value;
        return this;
    }

    public String
    getUrl() { return url; }
}

//////////////////////////////////////////////////////////////////////////////
