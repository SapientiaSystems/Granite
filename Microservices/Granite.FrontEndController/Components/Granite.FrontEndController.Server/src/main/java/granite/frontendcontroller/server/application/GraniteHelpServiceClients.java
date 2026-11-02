//////////////////////////////////////////////////////////////////////////////
// GraniteHelpServiceClients.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.application;

import granite.feedback.client.requestreply.IFeedbackCommandServiceClient;
import granite.feedback.client.requestreply.IFeedbackQueryServiceClient;
import granite.survey.client.requestreply.ISurveyCommandServiceClient;
import granite.survey.client.requestreply.ISurveyQueryServiceClient;

import java.util.Objects;
import java.util.function.Supplier;

public
class GraniteHelpServiceClients
{
    private Supplier<IFeedbackCommandServiceClient> feedbackCommandClient;
    private Supplier<IFeedbackQueryServiceClient>   feedbackQueryClient;
    private Supplier<ISurveyCommandServiceClient>   surveyCommandClient;
    private Supplier<ISurveyQueryServiceClient>     surveyQueryClient;

    public
    GraniteHelpServiceClients()
    {
        feedbackCommandClient = unavailable("feedback command");
        feedbackQueryClient   = unavailable("feedback query");
        surveyCommandClient   = unavailable("survey command");
        surveyQueryClient     = unavailable("survey query");
    }

    public GraniteHelpServiceClients
    setFeedbackCommandClient(Supplier<IFeedbackCommandServiceClient> supplier)
    {
        feedbackCommandClient = memoize(Objects.requireNonNull(supplier,"supplier"));
        return this;
    }

    public GraniteHelpServiceClients
    setFeedbackQueryClient(Supplier<IFeedbackQueryServiceClient> supplier)
    {
        feedbackQueryClient = memoize(Objects.requireNonNull(supplier,"supplier"));
        return this;
    }

    public GraniteHelpServiceClients
    setSurveyCommandClient(Supplier<ISurveyCommandServiceClient> supplier)
    {
        surveyCommandClient = memoize(Objects.requireNonNull(supplier,"supplier"));
        return this;
    }

    public GraniteHelpServiceClients
    setSurveyQueryClient(Supplier<ISurveyQueryServiceClient> supplier)
    {
        surveyQueryClient = memoize(Objects.requireNonNull(supplier,"supplier"));
        return this;
    }

    public IFeedbackCommandServiceClient
    getFeedbackCommandClient() { return feedbackCommandClient.get(); }

    public IFeedbackQueryServiceClient
    getFeedbackQueryClient() { return feedbackQueryClient.get(); }

    public ISurveyCommandServiceClient
    getSurveyCommandClient() { return surveyCommandClient.get(); }

    public ISurveyQueryServiceClient
    getSurveyQueryClient() { return surveyQueryClient.get(); }

    private static <T> Supplier<T>
    unavailable(String name)
    {
        return
            () ->
            {
                throw new IllegalStateException(name + " client is not configured");
            };
    }

    private static <T> Supplier<T>
    memoize(Supplier<T> supplier)
    {
        Object[] holder = new Object[1];

        return
            () ->
            {
                synchronized (holder)
                {
                    if (holder[0] == null)
                        holder[0] = Objects.requireNonNull(supplier.get(),"client");

                    @SuppressWarnings("unchecked")
                    T value = (T)holder[0];

                    return value;
                }
            };
    }
}

//////////////////////////////////////////////////////////////////////////////
