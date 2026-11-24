//////////////////////////////////////////////////////////////////////////////
// TokenPathRedactingRequestLoggingFilterTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.configuration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class TokenPathRedactingRequestLoggingFilterTest
{
    private final TokenPathRedactingRequestLoggingFilter filter = new TokenPathRedactingRequestLoggingFilter();

    private MockHttpServletRequest
    request(String uri,String query)
    {
        MockHttpServletRequest request = new MockHttpServletRequest("POST",uri);

        request.setQueryString(query);
        return request;
    }

    @Test
    public void
    tokenPathMessageNeverContainsTheQueryString()
    {
        MockHttpServletRequest request = request("/session-command-service/oauth2/token","client_secret=s3cr3t");
        String                 message = filter.createMessage(request,"REQUEST DATA: ","]");

        assertFalse(message.contains("s3cr3t"));
        assertFalse(message.contains("client_secret"));
    }

    @Test
    public void
    tokenPathIsNotLogged()
    {
        assertFalse(filter.shouldLog(request("/session-command-service/oauth2/token","client_secret=s3cr3t")));
    }

    @Test
    public void
    otherPathsKeepTheQueryString()
    {
        MockHttpServletRequest request = request("/session-query-service/find","a=b");

        filter.setIncludeQueryString(true);
        assertTrue(filter.createMessage(request,"P: ","]").contains("?a=b"));
    }
}

//////////////////////////////////////////////////////////////////////////////
