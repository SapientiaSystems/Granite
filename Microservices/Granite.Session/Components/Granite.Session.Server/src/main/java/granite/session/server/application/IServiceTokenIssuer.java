//////////////////////////////////////////////////////////////////////////////
// IServiceTokenIssuer.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.service.requestreply.IssueServiceTokenReply;
import granite.session.service.requestreply.IssueServiceTokenRequest;

public
interface IServiceTokenIssuer
{
    IssueServiceTokenReply
    issue(IssueServiceTokenRequest request);

    boolean
    mayDelegate(String clientId,String clientSecret);
}

//////////////////////////////////////////////////////////////////////////////
