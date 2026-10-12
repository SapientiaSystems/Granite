//////////////////////////////////////////////////////////////////////////////
// UserTestSuite.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.testsuite;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeTags({"CommitStage","IntegrationStage"})
@SelectPackages(
    {
        "granite.user.service.requestreply",
        "granite.user.server.application",
        "granite.user.server.domain",
        "granite.user.server.platform",
        "granite.user.client.requestreply",
        "granite.user.client.event"
    })
public
class UserTestSuite
{
}

//////////////////////////////////////////////////////////////////////////////
