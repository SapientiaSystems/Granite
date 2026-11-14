//////////////////////////////////////////////////////////////////////////////
// ClientTestSuite.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.testsuite;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeTags({"CommitStage","IntegrationStage"})
@SelectPackages(
    {
        "granite.client.service.requestreply",
        "granite.client.server.application",
        "granite.client.server.domain",
        "granite.client.server.platform",
        "granite.client.client.requestreply"
    })
public
class ClientTestSuite
{
}

//////////////////////////////////////////////////////////////////////////////
