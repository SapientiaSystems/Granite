//////////////////////////////////////////////////////////////////////////////
// TokenPathRedactingRequestLoggingFilter.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

public
class TokenPathRedactingRequestLoggingFilter
    extends CommonsRequestLoggingFilter
{
    public static final String TOKEN_PATH_SUFFIX = "/oauth2/token";

    @Override
    protected boolean
    shouldLog(HttpServletRequest request)
    {
        return !isTokenPath(request) && super.shouldLog(request);
    }

    @Override
    protected String
    createMessage(HttpServletRequest request,String prefix,String suffix)
    {
        if (isTokenPath(request))
            return prefix + "uri=" + request.getRequestURI() + suffix;

        return super.createMessage(request,prefix,suffix);
    }

    private boolean
    isTokenPath(HttpServletRequest request)
    {
        String uri = request.getRequestURI();

        return uri != null && uri.endsWith(TOKEN_PATH_SUFFIX);
    }
}

//////////////////////////////////////////////////////////////////////////////
