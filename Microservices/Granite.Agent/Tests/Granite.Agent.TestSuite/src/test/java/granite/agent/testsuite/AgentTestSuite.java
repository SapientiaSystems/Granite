//////////////////////////////////////////////////////////////////////////////
// AgentTestSuite.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.testsuite;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeTags({"CommitStage","IntegrationStage"})
@SelectPackages(
    {
        "granite.agent.service.requestreply",
        "granite.agent.server.application",
        "granite.agent.server.domain",
        "granite.agent.server.agent",
        "granite.agent.client.requestreply"
    })
public
class AgentTestSuite
{
}

//////////////////////////////////////////////////////////////////////////////
