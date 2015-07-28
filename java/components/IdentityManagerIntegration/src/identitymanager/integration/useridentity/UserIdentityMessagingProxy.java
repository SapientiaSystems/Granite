// ##########################################################################
// # File Name:	UserIdentityMessageProxy.java
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

package identitymanager.integration.useridentity;

import identitymanager.service.shared.OperationStatus;
import identitymanager.service.useridentity.FinishCreatingUserIdentityRequest;
import identitymanager.service.useridentity.FinishUpdatingUserIdentityRequest;
import identitymanager.service.useridentity.IUserIdentityEventListener;
import identitymanager.service.useridentity.IUserIdentityReplyReceiver;
import identitymanager.service.useridentity.StartCreatingUserIdentityReply;
import identitymanager.service.useridentity.StartCreatingUserIdentityRequest;
import identitymanager.service.useridentity.StartListeningReply;
import identitymanager.service.useridentity.StartListeningRequest;
import identitymanager.service.useridentity.StartUpdatingUserIdentityRequest;
import identitymanager.service.useridentity.StopListeningRequest;
import identitymanager.service.useridentity.UpdateUserIdentityReply;
import identitymanager.service.useridentity.UpdateUserIdentityRequest;
import identitymanager.service.useridentity.UserIdentityReply;
import identitymanager.integration.mapper.IEventMapper;
import identitymanager.integration.mapper.IReplyMapper;
import identitymanager.integration.mapper.IRequestMapper;
import identitymanager.common.utility.DefaultIdentifierGenerator;
import identitymanager.common.utility.IIdentifierGenerator;
import identitymanager.common.utility.ITimestampGenerator;
import strata1.common.logger.ILogger;
import strata1.injector.container.IContainer;
import strata1.integrator.messaging.IMessageSender;
import strata1.integrator.messaging.IMessagingSession;
import strata1.integrator.messaging.IStringMessage;
import strata1.integrator.messagingproxy.RequestReplyAndEventMessagingProxy;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class UserIdentityMessagingProxy
    extends    RequestReplyAndEventMessagingProxy<
                   String,
                   IUserIdentityReplyReceiver,
                   IUserIdentityEventListener>
    implements IUserIdentityMessagingProxy
{
    private final IRequestMapper       itsRequestMapper;
    private final IReplyMapper         itsReplyMapper;
    private final IEventMapper         itsEventMapper;
    private final ILogger              itsLogger;
    private final IIdentifierGenerator itsIdGenerator;
    private final ITimestampGenerator  itsTimeGenerator;
    
    /************************************************************************
     * Creates a new UserIdentityMessageProxy. 
     *
     * @param container
     */
    public
    UserIdentityMessagingProxy(IContainer container)
    {
        super(
            "UserIdentity-",
            container.getInstance(String.class,"RequestChannelId"),
            container.getInstance(String.class,"ReplyChannelId"),
            container.getInstance(String.class,"EventChannelId"),
            container.getInstance(IMessagingSession.class,"CommandSession"),
            container.getInstance(IMessagingSession.class,"EventSession"));
        
        itsLogger        = container.getInstance( ILogger.class );
        itsRequestMapper = container.getInstance( IRequestMapper.class );
        itsReplyMapper   = container.getInstance( IReplyMapper.class );
        itsEventMapper   = container.getInstance( IEventMapper.class );
        itsIdGenerator   = container.getInstance( IIdentifierGenerator.class );
        itsTimeGenerator = container.getInstance( ITimestampGenerator.class );
        
        activate();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    startCreatingUserIdentity(
        IUserIdentityReplyReceiver       receiver,
        StartCreatingUserIdentityRequest request)
    {
        IStringMessage message       = createStringMessage();
        IMessageSender sender        = createMessageSender();
        String         correlationId = request.getRequestId();
        
        try
        {
            request.setReturnAddress( getReturnAddress() );
            
            message
                .setCorrelationId( correlationId )
                .setStringProperty( 
                    "RequestType",
                    "StartCreatingUserIdentityRequest" )
                .setPayload( itsRequestMapper.toPayload( request ) );
    
            insertReplyReceiver( correlationId,receiver);
            itsLogger.logInfo( 
                "Sending start creating useridentity request: " + 
                request.getRequestId() );
            
            sender.setTimeToLive( 60*1000 );
            sender.send( message );
        }
        catch (Exception e)
        {
            receiver.onException( e );
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    finishCreatingUserIdentity(
        IUserIdentityReplyReceiver        receiver,
        FinishCreatingUserIdentityRequest request)
    {
        IStringMessage message       = createStringMessage();
        IMessageSender sender        = createMessageSender();
        String         correlationId = request.getRequestId();
        
        try
        {
            request.setReturnAddress( getReturnAddress() );
            
            message
                .setCorrelationId( correlationId )
                .setStringProperty( 
                    "RequestType",
                    "FinishCreatingUserIdentityRequest" )
                .setPayload( itsRequestMapper.toPayload( request ) );
    
            insertReplyReceiver( correlationId,receiver);
            itsLogger.logInfo( 
                "Sending finish creating useridentity request: " + 
                request.getRequestId() );
            
            sender.setTimeToLive( 60*1000 );
            sender.send( message );
        }
        catch (Exception e)
        {
            receiver.onException( e );
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    startUpdatingUserIdentity(
        IUserIdentityReplyReceiver       receiver,
        StartUpdatingUserIdentityRequest request)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    finishUpdatingUserIdentity(
        IUserIdentityReplyReceiver        receiver,
        FinishUpdatingUserIdentityRequest request)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    updateUserIdentity(
        IUserIdentityReplyReceiver receiver,
        UpdateUserIdentityRequest  request)
    {
        IStringMessage message       = createStringMessage();
        IMessageSender sender        = createMessageSender();
        String         correlationId = request.getRequestId();
        
        try
        {
            request.setReturnAddress( getReturnAddress() );

            message
                .setCorrelationId( correlationId )
                .setStringProperty( 
                    "RequestType",
                    "UpdateUserIdentityRequest" )
                .setPayload( itsRequestMapper.toPayload( request ) );
    
            insertReplyReceiver( correlationId,receiver);
            itsLogger.logInfo( 
                "Sending update useridentity request: " + 
                request.getRequestId() );
            
            sender.setTimeToLive( 60*1000 );
            sender.send( message );
        }
        catch (Exception e)
        {
            receiver.onException( e );
        }
    }
    
    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    startListening(
        IUserIdentityReplyReceiver receiver,
        StartListeningRequest      request)
    {
        IStringMessage message       = createStringMessage();
        IMessageSender sender        = createMessageSender();
        String         correlationId = request.getRequestId();
        
        try
        {
            if ( hasEventListener( getReturnAddress() ))
            {
                receiver.onStartListening( createReply(request) );
                return;
            }
            
            request.setReturnAddress( getReturnAddress() );

            message
                .setCorrelationId( correlationId )
                .setStringProperty( 
                    "RequestType",
                    "StartListeningRequest" )
                .setPayload( itsRequestMapper.toPayload( request ) );
    
            insertReplyReceiver( correlationId,receiver);
            itsLogger.logInfo( 
                "Sending start listening request: " + 
                request.getRequestId() );
            
            insertEventListener( getReturnAddress(),request.getListener() );
            sender.setTimeToLive( 60*1000 );
            sender.send( message );
        }
        catch (Exception e)
        {
            receiver.onException( e );
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    stopListening(
        IUserIdentityReplyReceiver receiver,
        StopListeningRequest       request)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    isListening()
    {
        return false;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onMessage(IStringMessage message)
    {
        String                     correlationId = null;
        String                     payload       = null;
        IUserIdentityReplyReceiver receiver      = null;
        UserIdentityReply          reply         = null;
        
        try
        {
            correlationId = message.getCorrelationId();
            payload       = message.getPayload();
            
            receiver = removeReplyReceiver(correlationId);
            reply    = itsReplyMapper.toReply( 
                           UserIdentityReply.class,
                           payload );
            
            if ( reply instanceof StartCreatingUserIdentityReply )
                receiver.onStartCreatingUserIdentity( 
                    (StartCreatingUserIdentityReply)reply );
            else if ( reply instanceof UpdateUserIdentityReply )
                receiver.onUpdateUserIdentity( 
                    (UpdateUserIdentityReply)reply );
            else 
                receiver.onException( 
                    new IllegalStateException(
                        "CorrelationId=" + correlationId + 
                        ": Unknown reply type.") );
        }
        catch (Exception e)
        {
            receiver.onException( e );
        }        
    }
    
    /************************************************************************
     *  
     *
     * @param request
     * @return
     */
    private StartListeningReply
    createReply(StartListeningRequest request)
    {
        StartListeningReply reply = new StartListeningReply();
        
        reply.setReplyId( itsIdGenerator.getNextLongId().toString() );
        reply.setOriginatingRequestId( request.getRequestId() );
        reply.setSessionId( request.getSessionId() );
        reply.setTimestamp( itsTimeGenerator.getCurrent() );
        reply.setDuplicate( false );
        reply.setStatus( OperationStatus.SUCCEEDED );
        
        return reply;
    }
    
}

// ##########################################################################
