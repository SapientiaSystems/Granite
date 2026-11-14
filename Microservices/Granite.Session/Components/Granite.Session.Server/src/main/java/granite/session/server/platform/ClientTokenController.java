//////////////////////////////////////////////////////////////////////////////
// ClientTokenController.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import granite.session.server.application.ClientTokenResult;
import granite.session.server.application.IClientTokenIssuer;
import jakarta.inject.Inject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@RestController
@RequestMapping("/session-command-service")
public
class ClientTokenController
{
    public static final String GRANT_TYPE              = "client_credentials";
    public static final String INVALID_REQUEST         = "invalid_request";
    public static final String UNSUPPORTED_GRANT       = "unsupported_grant_type";
    public static final String INVALID_CLIENT          = "invalid_client";
    public static final String TEMPORARILY_UNAVAILABLE = "temporarily_unavailable";
    public static final String CHALLENGE               = "Basic realm=\"granite\"";

    private final IClientTokenIssuer issuer;

    @Inject
    public
    ClientTokenController(IClientTokenIssuer issuer)
    {
        this.issuer = issuer;
    }

    @PostMapping(
        value = "/oauth2/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletionStage<ResponseEntity<Map<String,Object>>>
    token(
        @RequestHeader(value = "Authorization",required = false) String authorization,
        @RequestParam MultiValueMap<String,String>                 form)
    {
        List<String> grants  = form.get("grant_type");
        List<String> ids     = form.get("client_id");
        List<String> secrets = form.get("client_secret");
        boolean      basic   = authorization != null && authorization.regionMatches(true,0,"Basic ",0,6);
        boolean      fields  = ids != null || secrets != null;

        if (grants == null || grants.size() != 1 || grants.get(0).isBlank()
            || (ids != null && ids.size() != 1) || (secrets != null && secrets.size() != 1))
            return immediate(error(HttpStatus.BAD_REQUEST,INVALID_REQUEST));

        if (!GRANT_TYPE.equals(grants.get(0)))
            return immediate(error(HttpStatus.BAD_REQUEST,UNSUPPORTED_GRANT));

        String[] credentials;

        if (basic)
        {
            if (fields)
                return immediate(error(HttpStatus.BAD_REQUEST,INVALID_REQUEST));

            credentials = parseBasic(authorization.substring(6).trim());
        }
        else if (ids != null && secrets != null)
            credentials = new String[] {ids.get(0),secrets.get(0)};
        else
            credentials = null;

        if (credentials == null || credentials[0].isBlank() || credentials[1].isBlank())
            return immediate(error(HttpStatus.BAD_REQUEST,INVALID_REQUEST));

        return
            issuer
                .issue(credentials[0],credentials[1])
                .handle((result,failure) -> respond(failure == null ? result : ClientTokenResult.unavailable()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String,Object>>
    unsupportedMediaType()
    {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE,INVALID_REQUEST);
    }

    private ResponseEntity<Map<String,Object>>
    respond(ClientTokenResult result)
    {
        if (result == null)
            return error(HttpStatus.SERVICE_UNAVAILABLE,TEMPORARILY_UNAVAILABLE);

        switch (result.getOutcome())
        {
            case OK:
                Map<String,Object> body = new LinkedHashMap<>();

                body.put("access_token",result.getToken());
                body.put("token_type","Bearer");
                body.put("expires_in",result.getExpiresInSeconds());
                return
                    ResponseEntity
                        .ok()
                        .header(HttpHeaders.CACHE_CONTROL,"no-store")
                        .header(HttpHeaders.PRAGMA,"no-cache")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body);

            case INVALID_CLIENT:
                return
                    ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE,CHALLENGE)
                        .header(HttpHeaders.CACHE_CONTROL,"no-store")
                        .header(HttpHeaders.PRAGMA,"no-cache")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("error",INVALID_CLIENT));

            default:
                return error(HttpStatus.SERVICE_UNAVAILABLE,TEMPORARILY_UNAVAILABLE);
        }
    }

    private String[]
    parseBasic(String encoded)
    {
        try
        {
            String decoded = new String(Base64.getDecoder().decode(encoded),StandardCharsets.UTF_8);
            int    colon   = decoded.indexOf(':');

            if (colon < 0)
                return null;

            return
                new String[]
                {
                    URLDecoder.decode(decoded.substring(0,colon),StandardCharsets.UTF_8),
                    URLDecoder.decode(decoded.substring(colon + 1),StandardCharsets.UTF_8)
                };
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    private ResponseEntity<Map<String,Object>>
    error(HttpStatus status,String error)
    {
        return
            ResponseEntity
                .status(status)
                .header(HttpHeaders.CACHE_CONTROL,"no-store")
                .header(HttpHeaders.PRAGMA,"no-cache")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error",error));
    }

    private CompletionStage<ResponseEntity<Map<String,Object>>>
    immediate(ResponseEntity<Map<String,Object>> response)
    {
        return CompletableFuture.completedFuture(response);
    }
}

//////////////////////////////////////////////////////////////////////////////
