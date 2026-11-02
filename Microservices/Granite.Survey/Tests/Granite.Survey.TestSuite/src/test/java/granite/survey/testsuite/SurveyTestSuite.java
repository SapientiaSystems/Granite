//////////////////////////////////////////////////////////////////////////////
// SurveyTestSuite.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.testsuite;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeTags({"CommitStage","IntegrationStage"})
@SelectPackages(
    {
        "granite.survey.service.requestreply",
        "granite.survey.server.application",
        "granite.survey.server.domain",
        "granite.survey.client.requestreply"
    })
public
class SurveyTestSuite
{
}

//////////////////////////////////////////////////////////////////////////////
