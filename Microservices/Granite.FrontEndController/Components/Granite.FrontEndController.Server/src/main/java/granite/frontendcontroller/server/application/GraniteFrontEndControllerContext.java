//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.frontendcontroller.server.control.session.SessionLoginSettings;
import granite.frontendcontroller.server.platform.SessionCookies;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.session.client.requestreply.ISessionCommandServiceClient;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import org.modelmapper.ModelMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
    private final SessionCookies               sessionCookies;
    private final ApplicationRolesManager applicationRoles;
    private final List<PreferenceDefinition>   preferenceDefinitions;
    private final GraniteHelpServiceClients    helpServiceClients;

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
        SessionCookies               sessionCookies,
        ApplicationRolesManager applicationRoles,
        List<PreferenceDefinition>   preferenceDefinitions)
    {
        this(
            mapper,
            executor,
            userQueryClient,
            userCommandClient,
            sessionCommandClient,
            sessionQueryClient,
            sessionRequestContext,
            sessionLoginSettings,
            sessionCookies,
            applicationRoles,
            preferenceDefinitions,
            new GraniteHelpServiceClients());
    }

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
        SessionCookies               sessionCookies,
        ApplicationRolesManager      applicationRoles,
        List<PreferenceDefinition>   preferenceDefinitions,
        GraniteHelpServiceClients    helpServiceClients)
    {
        this.mapper                = Objects.requireNonNull(mapper,"mapper");
        this.executor              = Objects.requireNonNull(executor,"executor");
        this.userQueryClient       = Objects.requireNonNull(userQueryClient,"userQueryClient");
        this.userCommandClient     = Objects.requireNonNull(userCommandClient,"userCommandClient");
        this.sessionCommandClient  = Objects.requireNonNull(sessionCommandClient,"sessionCommandClient");
        this.sessionQueryClient    = Objects.requireNonNull(sessionQueryClient,"sessionQueryClient");
        this.sessionRequestContext = Objects.requireNonNull(sessionRequestContext,"sessionRequestContext");
        this.sessionLoginSettings  = Objects.requireNonNull(sessionLoginSettings,"sessionLoginSettings");
        this.sessionCookies        = Objects.requireNonNull(sessionCookies,"sessionCookies");
        this.applicationRoles      = Objects.requireNonNull(applicationRoles,"applicationRoles");
        this.preferenceDefinitions = List.copyOf(Objects.requireNonNull(preferenceDefinitions,"preferenceDefinitions"));
        this.helpServiceClients    = Objects.requireNonNull(helpServiceClients,"helpServiceClients");

        Set<String> keys = new HashSet<>();

        for (PreferenceDefinition definition : this.preferenceDefinitions)
            if (!keys.add(definition.getKey()))
                throw new IllegalArgumentException("duplicate preference key: " + definition.getKey());
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

    public SessionCookies
    getSessionCookies() { return sessionCookies; }

    public ApplicationRolesManager
    getApplicationRoles() { return applicationRoles; }

    public List<PreferenceDefinition>
    getPreferenceDefinitions() { return preferenceDefinitions; }

    public GraniteHelpServiceClients
    getHelpServiceClients() { return helpServiceClients; }
}

//////////////////////////////////////////////////////////////////////////////
