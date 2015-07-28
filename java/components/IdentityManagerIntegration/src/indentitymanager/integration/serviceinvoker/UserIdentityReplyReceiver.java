package indentitymanager.integration.serviceinvoker;

import identitymanager.integration.mapper.IReplyMapper;
import identitymanager.service.useridentity.FinishCreatingUserIdentityReply;
import identitymanager.service.useridentity.FinishUpdatingUserIdentityReply;
import identitymanager.service.useridentity.IUserIdentityReplyReceiver;
import identitymanager.service.useridentity.StartCreatingUserIdentityReply;
import identitymanager.service.useridentity.StartListeningReply;
import identitymanager.service.useridentity.StartUpdatingUserIdentityReply;
import identitymanager.service.useridentity.StopListeningReply;
import identitymanager.service.useridentity.UpdateUserIdentityReply;
import identitymanager.service.useridentity.UserIdentityException;
import strata1.common.logger.ILogger;
import strata1.integrator.messaging.IMessagingSession;

public 
class UserIdentityReplyReceiver
    implements IUserIdentityReplyReceiver
{

    /************************************************************************
     * Creates a new UserIdentityReplyReceiver. 
     *
     * @param commandSession
     * @param replyChannel
     * @param returnAddress
     * @param correlationId
     * @param replyMapper
     * @param logger
     */
    public 
    UserIdentityReplyReceiver(
        IMessagingSession commandSession,
        String            replyChannel,
        String            returnAddress,
        String            correlationId,
        IReplyMapper      replyMapper,
        ILogger           logger)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onStartCreatingUserIdentity(StartCreatingUserIdentityReply reply)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onFinishCreatingUserIdentity(FinishCreatingUserIdentityReply reply)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onStartUpdatingUserIdentity(StartUpdatingUserIdentityReply reply)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onFinishUpdatingUserIdentity(FinishUpdatingUserIdentityReply reply)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onUpdateUserIdentity(UpdateUserIdentityReply reply)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onStartListening(StartListeningReply reply)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onStopListening(StopListeningReply reply)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onUserIdentityException(UserIdentityException exception)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onException(Exception exception)
    {
    }

}

// ##########################################################################
