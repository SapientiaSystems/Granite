//////////////////////////////////////////////////////////////////////////////
// ResponseCookieAdvice.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;

@ControllerAdvice
public
class ResponseCookieAdvice
    implements ResponseBodyAdvice<Object>
{
    @Override
    public boolean
    supports(MethodParameter returnType,Class<? extends HttpMessageConverter<?>> converterType)
    {
        return true;
    }

    @Override
    public Object
    beforeBodyWrite(
        Object                                  body,
        MethodParameter                         returnType,
        MediaType                               selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType,
        ServerHttpRequest                       request,
        ServerHttpResponse                      response)
    {
        if (request instanceof ServletServerHttpRequest servlet
            && servlet.getServletRequest().getAttribute(SessionRequestContext.ATTRIBUTE) instanceof SessionRequestContext context)
        {
            List<String> cookies = context.getSetCookies();

            if (!cookies.isEmpty())
            {
                for (String cookie : cookies)
                    response.getHeaders().add(HttpHeaders.SET_COOKIE,cookie);

                response.getHeaders().setCacheControl("no-store");
            }
        }

        return body;
    }
}

//////////////////////////////////////////////////////////////////////////////
