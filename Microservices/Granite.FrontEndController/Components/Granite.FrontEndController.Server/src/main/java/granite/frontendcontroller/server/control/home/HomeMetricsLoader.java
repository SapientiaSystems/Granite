//////////////////////////////////////////////////////////////////////////////
// HomeMetricsLoader.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.home;

import granite.feedback.service.requestreply.FindAllFeedbackReply;
import granite.feedback.service.requestreply.FindAllFeedbackRequest;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.service.requestreply.HomeOutput;
import granite.session.client.requestreply.ISessionQueryServiceClient;
import granite.session.service.requestreply.FindSessionsReply;
import granite.session.service.requestreply.FindSessionsRequest;
import granite.survey.service.requestreply.FindAllSurveyResponsesReply;
import granite.survey.service.requestreply.FindAllSurveyResponsesRequest;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.service.requestreply.FindAllUsersReply;
import granite.user.service.requestreply.FindAllUsersRequest;
import granite.user.service.requestreply.UserData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public
class HomeMetricsLoader
{
    private final GraniteHelpServiceClients  clients;
    private final IUserQueryServiceClient    userQueryClient;
    private final ISessionQueryServiceClient sessionQueryClient;
    private final HomeMetricsCalculator      calculator;

    public
    HomeMetricsLoader(
        GraniteHelpServiceClients  clients,
        IUserQueryServiceClient    userQueryClient,
        ISessionQueryServiceClient sessionQueryClient,
        HomeMetricsCalculator      calculator)
    {
        this.clients            = clients;
        this.userQueryClient    = userQueryClient;
        this.sessionQueryClient = sessionQueryClient;
        this.calculator         = calculator;
    }

    public CompletionStage<HomeOutput>
    load(Long callerUserId,Set<String> callerAuthorities)
    {
        List<String> failures = Collections.synchronizedList(new ArrayList<>());
        HomeOutput   output   =
            new HomeOutput()
                .setRoles(calculator.roleLabels(callerAuthorities))
                .setGenerated(calculator.now().toString());

        CompletableFuture<Void> users =
            read(() -> userQueryClient.findAllUsers(new FindAllUsersRequest()),"users",failures)
                .thenAccept(
                    reply ->
                    {
                        if (succeeded(reply == null ? null : reply.isSuccess(),reply == null ? null : reply.getFailureMessage(),"users",failures))
                        {
                            List<UserData> all = reply.getUsers() == null ? List.of() : reply.getUsers();

                            output.setUsers(calculator.users(all)).setUserName(userName(all,callerUserId));
                        }
                    });
        CompletableFuture<Void> sessions =
            read(() -> sessionQueryClient.findSessions(new FindSessionsRequest().setIncludeClosed(true)),"sessions",failures)
                .thenAccept(
                    reply ->
                    {
                        if (succeeded(reply == null ? null : reply.isSuccess(),reply == null ? null : reply.getFailureMessage(),"sessions",failures))
                            output.setSessions(calculator.sessions(reply.getSessions() == null ? List.of() : reply.getSessions()));
                    });
        CompletableFuture<Void> feedback =
            read(() -> clients.getFeedbackQueryClient().findAllFeedback(new FindAllFeedbackRequest()),"feedback",failures)
                .thenAccept(
                    reply ->
                    {
                        if (succeeded(reply == null ? null : reply.isSuccess(),reply == null ? null : reply.getFailureMessage(),"feedback",failures))
                            output.setFeedback(calculator.feedback(reply.getFeedback() == null ? List.of() : reply.getFeedback()));
                    });
        CompletableFuture<Void> survey =
            read(() -> clients.getSurveyQueryClient().findAllSurveyResponses(new FindAllSurveyResponsesRequest()),"survey responses",failures)
                .thenAccept(
                    reply ->
                    {
                        if (succeeded(reply == null ? null : reply.isSuccess(),reply == null ? null : reply.getFailureMessage(),"survey responses",failures))
                            output.setSurvey(calculator.survey(reply.getResponses() == null ? List.of() : reply.getResponses()));
                    });

        return
            CompletableFuture
                .allOf(users,sessions,feedback,survey)
                .thenApply(v -> output.setFailureMessages(new ArrayList<>(failures)));
    }

    protected <T> CompletableFuture<T>
    read(Supplier<CompletionStage<T>> call,String subject,List<String> failures)
    {
        CompletionStage<T> stage;

        try
        {
            stage = call.get();
        }
        catch (RuntimeException e)
        {
            stage = CompletableFuture.failedFuture(e);
        }

        return
            stage
                .toCompletableFuture()
                .exceptionally(
                    error ->
                    {
                        failures.add(failure(subject,error.getMessage()));
                        return null;
                    });
    }

    private boolean
    succeeded(Boolean success,String message,String subject,List<String> failures)
    {
        if (success == null)
            return false;

        if (!success)
            failures.add(failure(subject,message));

        return success;
    }

    private String
    userName(List<UserData> users,Long callerUserId)
    {
        for (UserData user : users)
            if (Objects.equals(user.getUserId(),callerUserId))
                return user.getUserName();

        return null;
    }

    private String
    failure(String subject,String message)
    {
        return "Unable to read " + subject + ": " + (message == null || message.isBlank() ? "unknown error" : message);
    }
}

//////////////////////////////////////////////////////////////////////////////
