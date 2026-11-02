//////////////////////////////////////////////////////////////////////////////
// FeedbackTestSuite.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.testsuite;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeTags({"CommitStage","IntegrationStage"})
@SelectPackages(
    {
        "granite.feedback.service.requestreply",
        "granite.feedback.server.application",
        "granite.feedback.server.domain",
        "granite.feedback.client.requestreply"
    })
public
class FeedbackTestSuite
{
}

//////////////////////////////////////////////////////////////////////////////
