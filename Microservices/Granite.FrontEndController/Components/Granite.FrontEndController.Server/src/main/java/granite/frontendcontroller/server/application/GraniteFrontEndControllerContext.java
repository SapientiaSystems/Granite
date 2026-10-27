//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import granite.foundation.core.security.ApplicationRoles;
import granite.frontendcontroller.server.control.session.SessionLoginSettings;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.modelmapper.ModelMapper;

import java.util.Objects;
import java.util.concurrent.Executor;

public
class GraniteFrontEndControllerContext
{
    private final ModelMapper                  mapper;
    private final Executor                     executor;
    private final IUserQueryServiceClient      userQueryClient;
    private final IUserCommandServiceClient    userCommandClient;
    private final ISessionCommandServiceClient sessionCommandClient;
    private final ISessionQueryServiceClient   sessionQueryClient;
    private final SessionRequestContext        sessionRequestContext;
    private final SessionLoginSettings         sessionLoginSettings;
    private final ApplicationRoles             applicationRoles;

    public
    GraniteFrontEndControllerContext(
        ModelMapper                  mapper,
        Executor                     executor,
        IUserQueryServiceClient      userQueryClient,
        IUserCommandServiceClient    userCommandClient,
        ISessionCommandServiceClient sessionCommandClient,
        ISessionQueryServiceClient   sessionQueryClient,
        SessionRequestContext        sessionRequestContext,
        SessionLoginSettings         sessionLoginSettings,
        ApplicationRoles             applicationRoles)
    {
        this.mapper                = Objects.requireNonNull(mapper,"mapper");
        this.executor              = Objects.requireNonNull(executor,"executor");
        this.userQueryClient       = Objects.requireNonNull(userQueryClient,"userQueryClient");
        this.userCommandClient     = Objects.requireNonNull(userCommandClient,"userCommandClient");
        this.sessionCommandClient  = Objects.requireNonNull(sessionCommandClient,"sessionCommandClient");
        this.sessionQueryClient    = Objects.requireNonNull(sessionQueryClient,"sessionQueryClient");
        this.sessionRequestContext = Objects.requireNonNull(sessionRequestContext,"sessionRequestContext");
        this.sessionLoginSettings  = Objects.requireNonNull(sessionLoginSettings,"sessionLoginSettings");
        this.applicationRoles      = Objects.requireNonNull(applicationRoles,"applicationRoles");
    }

    public ModelMapper
    getMapper() { return mapper; }

    public Executor
    getExecutor() { return executor; }

    public IUserQueryServiceClient
    getUserQueryClient() { return userQueryClient; }

    public IUserCommandServiceClient
    getUserCommandClient() { return userCommandClient; }

    public ISessionCommandServiceClient
    getSessionCommandClient() { return sessionCommandClient; }

    public ISessionQueryServiceClient
    getSessionQueryClient() { return sessionQueryClient; }

    public SessionRequestContext
    getSessionRequestContext() { return sessionRequestContext; }

    public SessionLoginSettings
    getSessionLoginSettings() { return sessionLoginSettings; }

    public ApplicationRoles
    getApplicationRoles() { return applicationRoles; }
}

//////////////////////////////////////////////////////////////////////////////
