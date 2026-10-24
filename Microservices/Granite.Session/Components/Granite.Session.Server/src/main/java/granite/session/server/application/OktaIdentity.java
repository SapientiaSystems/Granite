//////////////////////////////////////////////////////////////////////////////
// OktaIdentity.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import java.util.LinkedHashSet;
import java.util.Set;

public
class OktaIdentity
{
    private final String      subject;
    private final String      userName;
    private final Set<String> groups;
    private final String      idToken;

    public
    OktaIdentity(String subject,String userName,Set<String> groups,String idToken)
    {
        this.subject  = subject;
        this.userName = userName;
        this.groups   = groups != null ? new LinkedHashSet<>(groups) : new LinkedHashSet<>();
        this.idToken  = idToken;
    }

    public String
    getSubject() { return subject; }

    public String
    getUserName() { return userName; }

    public Set<String>
    getGroups() { return groups; }

    public String
    getIdToken() { return idToken; }

    @Override
    public String
    toString()
    {
        return "OktaIdentity{subject=" + subject + ",groups=" + groups.size() + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
