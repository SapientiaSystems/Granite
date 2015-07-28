// ##########################################################################
// # File Name:	ServiceInvoker.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerIntegration Framework.
// #
// #   			The IdentityManagerIntegration Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerIntegration Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerIntegration
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package indentitymanager.integration.serviceinvoker;

import identitymanager.integration.mapper.IReplyMapper;
import identitymanager.integration.mapper.IRequestMapper;
import identitymanager.service.shared.IdentityManagerRequest;
import identitymanager.service.useridentity.FinishCreatingUserIdentityRequest;
import identitymanager.service.useridentity.IUserIdentityReplyReceiver;
import identitymanager.service.useridentity.StartCreatingUserIdentityRequest;
import identitymanager.service.useridentity.UserIdentityRequest;
import strata1.common.logger.ILogger;
import strata1.common.task.AbstractTaskProducer;
import strata1.common.task.ITaskDispatcher;
import strata1.common.task.ITaskProducer;
import strata1.injector.container.IContainer;
import strata1.integrator.messaging.IBytesMessage;
import strata1.integrator.messaging.IMapMessage;
import strata1.integrator.messaging.IMessageListener;
import strata1.integrator.messaging.IMessageReceiver;
import strata1.integrator.messaging.IMessagingSession;
import strata1.integrator.messaging.IObjectMessage;
import strata1.integrator.messaging.IStringMessage;
import strata1.integrator.messaging.MixedModeException;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class ServiceInvoker
    extends    AbstractTaskProducer
    implements ITaskProducer,IMessageListener
{
    private final IContainer        itsContainer;
    private final String            itsRequestChannel;
    private final String            itsReplyChannel;
    private final String            itsEventChannel;
    private final IMessagingSession itsCommandSession;
    private final IMessagingSession itsEventSession;
    private final ILogger           itsLogger;
    private IMessageReceiver        itsReceiver;
    private final IRequestMapper    itsRequestMapper;
    private final IReplyMapper      itsReplyMapper;

    /************************************************************************
     * Creates a new SessionInvoker. 
     *
     */
    public 
    ServiceInvoker(
        IContainer container,
        String     commandSession,
        String     requestChannelId,
        String     replyChannelId,
        String     eventSession,
        String     eventChannelId)
    {        
        itsContainer = container;
        itsRequestChannel = 
            itsContainer.getInstance( String.class,requestChannelId );
        itsReplyChannel = 
            itsContainer.getInstance( String.class,replyChannelId );
        itsEventChannel = 
            itsContainer.getInstance( String.class,eventChannelId );
        itsCommandSession  = 
            itsContainer.getInstance( IMessagingSession.class,commandSession );
        itsEventSession  = 
            itsContainer.getInstance( IMessagingSession.class,eventSession );
        itsLogger = 
            itsContainer.getInstance(  ILogger.class );
        itsReceiver = null;
        itsRequestMapper = 
            itsContainer.getInstance( IRequestMapper.class );
        itsReplyMapper = 
            itsContainer.getInstance( IReplyMapper.class );
        
        setDispatcher(itsContainer.getInstance(ITaskDispatcher.class));
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    startProducing()
    {        
        try
        {
            if ( itsReceiver == null )
            {
                itsReceiver = 
                    itsCommandSession.createMessageReceiver(itsRequestChannel);
                itsReceiver.setListener( this );
            }
            
            itsReceiver.startListening();
        }
        catch (MixedModeException e)
        {
            throw new IllegalStateException( e );
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    stopProducing()
    {
        if ( itsReceiver != null ) 
        {
            try
            {
                itsReceiver.stopListening();
                itsReceiver.close();
                itsReceiver = null;
            }
            catch (MixedModeException e)
            {
                throw new IllegalStateException( e );
            }
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    isProducing()
    {
        return itsReceiver.isListening();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onMessage(IStringMessage message)
    {
        String                 payload = null;
        IdentityManagerRequest request = null;
        
        try
        {
            payload   = message.getPayload();
            request = itsRequestMapper.toRequest( 
                IdentityManagerRequest.class,payload );
            
            if ( request instanceof UserIdentityRequest ) 
                dispatchUserIdentityRequest( message,request );
            {
                itsLogger.logError( 
                    "Unknown request type: "  + 
                    request.getClass().getSimpleName() );
            }               
        }
        catch (Exception e)
        {
            itsLogger.logError( e.getMessage() );
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onMessage(IMapMessage message)
    {
        throw new UnsupportedOperationException();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onMessage(IObjectMessage message)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onMessage(IBytesMessage arg0)
    {
        throw new UnsupportedOperationException();
    }

    /************************************************************************
     *  
     *
     * @param message
     * @param request
     */
    private void 
    dispatchUserIdentityRequest(
        IStringMessage         message,
        IdentityManagerRequest request)
    {
        String                     requestId = request.getRequestId();
        IUserIdentityReplyReceiver receiver = 
            new UserIdentityReplyReceiver(
                itsCommandSession,
                itsReplyChannel,
                message.getReturnAddress(),
                message.getCorrelationId(),
                itsReplyMapper,
                itsLogger); 

        itsLogger.logInfo( 
            "Receiving message: " + message.getMessageId() );
        
        if ( request instanceof StartCreatingUserIdentityRequest )
        {
            itsLogger.logInfo( 
                "Receiving start creating useridentity request: " + 
                requestId );
            getDispatcher()
                .route(
                    new StartCreatingUserIdentityTask(
                        itsContainer,
                        (StartCreatingUserIdentityRequest)request,
                        receiver ) );
        }
        else if ( request instanceof FinishCreatingUserIdentityRequest )
        {
            itsLogger.logInfo( 
                "Receiving finish creating useridentity request: " + 
                requestId );
            getDispatcher()
                .route(
                    new FinishCreatingUserIdentityTask(
                        itsContainer,
                        (FinishCreatingUserIdentityRequest)request,
                        receiver ) );
        }
        else
        {
            itsLogger.logError( 
                "Unknown payload type: "  + 
                request.getClass().getSimpleName() );
        }
    }
}

// ##########################################################################
