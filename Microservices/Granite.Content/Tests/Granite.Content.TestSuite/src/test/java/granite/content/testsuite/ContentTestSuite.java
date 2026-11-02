//////////////////////////////////////////////////////////////////////////////
// ContentTestSuite.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.testsuite;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeTags({"CommitStage","IntegrationStage"})
@SelectPackages(
    {
        "granite.content.service.requestreply",
        "granite.content.server.application",
        "granite.content.server.domain",
        "granite.content.client.requestreply"
    })
public
class ContentTestSuite
{
}

//////////////////////////////////////////////////////////////////////////////
