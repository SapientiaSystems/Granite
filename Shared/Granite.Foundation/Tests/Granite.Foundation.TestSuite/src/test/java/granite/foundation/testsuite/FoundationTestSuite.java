//////////////////////////////////////////////////////////////////////////////
// FoundationTestSuite.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.testsuite;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeTags({"CommitStage","IntegrationStage"})
@SelectPackages(
    {
        "granite.foundation.core.mapper"
    })
public
class FoundationTestSuite
{
}

//////////////////////////////////////////////////////////////////////////////
