//////////////////////////////////////////////////////////////////////////////
// SessionTestSuite.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.testsuite;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeTags({"CommitStage","IntegrationStage"})
@SelectPackages(
    {
        "granite.session.service.requestreply",
        "granite.session.server.application",
        "granite.session.server.domain",
        "granite.session.server.platform",
        "granite.session.client.requestreply",
        "granite.session.client.event"
    })
public
class SessionTestSuite
{
}

//////////////////////////////////////////////////////////////////////////////
