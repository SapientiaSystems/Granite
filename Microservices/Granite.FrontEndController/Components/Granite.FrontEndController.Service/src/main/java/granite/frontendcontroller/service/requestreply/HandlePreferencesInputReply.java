//////////////////////////////////////////////////////////////////////////////
// HandlePreferencesInputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.HandleInputReply;

import java.time.Instant;
import java.util.UUID;

public
class HandlePreferencesInputReply
    extends HandleInputReply<PreferencesInput,PreferencesOutput>
{
    public
    HandlePreferencesInputReply()
    {
        super();
    }

    public
    HandlePreferencesInputReply(HandlePreferencesInputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public HandlePreferencesInputReply
    setReplyId(UUID replyId)
    {
        return (HandlePreferencesInputReply)super.setReplyId(replyId);
    }

    @Override
    public HandlePreferencesInputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (HandlePreferencesInputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public HandlePreferencesInputReply
    setTimestamp(Instant timestamp)
    {
        return (HandlePreferencesInputReply)super.setTimestamp(timestamp);
    }

    @Override
    public HandlePreferencesInputReply
    setSuccess(boolean success)
    {
        return (HandlePreferencesInputReply)super.setSuccess(success);
    }

    @Override
    public HandlePreferencesInputReply
    setSuccessMessage(String successMessage)
    {
        return (HandlePreferencesInputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public HandlePreferencesInputReply
    setFailureMessage(String failureMessage)
    {
        return (HandlePreferencesInputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public HandlePreferencesInputReply
    setException(ExceptionData exception)
    {
        return (HandlePreferencesInputReply)super.setException(exception);
    }

    @Override
    public HandlePreferencesInputReply
    setInput(PreferencesInput input)
    {
        return (HandlePreferencesInputReply)super.setInput(input);
    }

    @Override
    public HandlePreferencesInputReply
    setOutput(PreferencesOutput output)
    {
        return (HandlePreferencesInputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
