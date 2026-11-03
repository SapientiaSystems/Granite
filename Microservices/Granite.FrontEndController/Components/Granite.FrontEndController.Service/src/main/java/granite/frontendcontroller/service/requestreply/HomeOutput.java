//////////////////////////////////////////////////////////////////////////////
// HomeOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndOutput;

import java.util.ArrayList;
import java.util.List;

public
class HomeOutput
    extends FrontEndOutput
{
    private String              userName;
    private List<String>        roles;
    private String              generated;
    private HomeUserMetrics     users;
    private HomeSessionMetrics  sessions;
    private HomeFeedbackMetrics feedback;
    private HomeSurveyMetrics   survey;
    private List<String>        failureMessages;

    public
    HomeOutput()
    {
        super();
        userName        = null;
        roles           = new ArrayList<>();
        generated       = null;
        users           = null;
        sessions        = null;
        feedback        = null;
        survey          = null;
        failureMessages = new ArrayList<>();
    }

    @Override
    public HomeOutput
    setUserId(Long id)
    {
        return (HomeOutput)super.setUserId(id);
    }

    @Override
    public HomeOutput
    setSource(String src)
    {
        return (HomeOutput)super.setSource(src);
    }

    @Override
    public HomeOutput
    setDestination(String dest)
    {
        return (HomeOutput)super.setDestination(dest);
    }

    @Override
    public HomeOutput
    setCommand(String cmd)
    {
        return (HomeOutput)super.setCommand(cmd);
    }

    public HomeOutput
    setUserName(String name)
    {
        userName = name;
        return this;
    }

    public HomeOutput
    setRoles(List<String> r)
    {
        roles = r;
        return this;
    }

    public HomeOutput
    setGenerated(String g)
    {
        generated = g;
        return this;
    }

    public HomeOutput
    setUsers(HomeUserMetrics u)
    {
        users = u;
        return this;
    }

    public HomeOutput
    setSessions(HomeSessionMetrics s)
    {
        sessions = s;
        return this;
    }

    public HomeOutput
    setFeedback(HomeFeedbackMetrics f)
    {
        feedback = f;
        return this;
    }

    public HomeOutput
    setSurvey(HomeSurveyMetrics s)
    {
        survey = s;
        return this;
    }

    public HomeOutput
    setFailureMessages(List<String> messages)
    {
        failureMessages = messages;
        return this;
    }

    public String
    getUserName() { return userName; }

    public List<String>
    getRoles() { return roles; }

    public String
    getGenerated() { return generated; }

    public HomeUserMetrics
    getUsers() { return users; }

    public HomeSessionMetrics
    getSessions() { return sessions; }

    public HomeFeedbackMetrics
    getFeedback() { return feedback; }

    public HomeSurveyMetrics
    getSurvey() { return survey; }

    public List<String>
    getFailureMessages() { return failureMessages; }
}

//////////////////////////////////////////////////////////////////////////////
