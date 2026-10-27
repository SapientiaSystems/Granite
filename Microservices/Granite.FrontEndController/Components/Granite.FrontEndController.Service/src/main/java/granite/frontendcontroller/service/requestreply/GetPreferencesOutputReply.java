//////////////////////////////////////////////////////////////////////////////
// GetPreferencesOutputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.GetOutputReply;

import java.time.Instant;
import java.util.UUID;

public
class GetPreferencesOutputReply
    extends GetOutputReply<PreferencesOutput>
{
    public
    GetPreferencesOutputReply()
    {
        super();
    }

    public
    GetPreferencesOutputReply(GetPreferencesOutputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public GetPreferencesOutputReply
    setReplyId(UUID replyId)
    {
        return (GetPreferencesOutputReply)super.setReplyId(replyId);
    }

    @Override
    public GetPreferencesOutputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (GetPreferencesOutputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public GetPreferencesOutputReply
    setTimestamp(Instant timestamp)
    {
        return (GetPreferencesOutputReply)super.setTimestamp(timestamp);
    }

    @Override
    public GetPreferencesOutputReply
    setSuccess(boolean success)
    {
        return (GetPreferencesOutputReply)super.setSuccess(success);
    }

    @Override
    public GetPreferencesOutputReply
    setSuccessMessage(String successMessage)
    {
        return (GetPreferencesOutputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public GetPreferencesOutputReply
    setFailureMessage(String failureMessage)
    {
        return (GetPreferencesOutputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public GetPreferencesOutputReply
    setException(ExceptionData exception)
    {
        return (GetPreferencesOutputReply)super.setException(exception);
    }

    @Override
    public GetPreferencesOutputReply
    setOutput(PreferencesOutput output)
    {
        return (GetPreferencesOutputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
