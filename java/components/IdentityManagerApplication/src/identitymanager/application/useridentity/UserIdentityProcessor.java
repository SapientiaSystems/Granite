// ##########################################################################
// # File Name:	UserIdentityProcessor.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerApplication Framework.
// #
// #   			The IdentityManagerApplication Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerApplication Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerApplication
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.application.useridentity;

import identitymanager.domain.useridentity.IUserIdentity;
import identitymanager.domain.useridentity.IUserIdentityEvent;
import identitymanager.domain.useridentity.IUserIdentityEventRepository;
import identitymanager.domain.useridentity.IUserIdentityRepository;
import identitymanager.domain.useridentity.UserIdentityStateKind;
import identitymanager.domain.useridentity.VerificationException;
import identitymanager.common.value.EmailAddress;
import identitymanager.service.shared.OperationStatus;
import identitymanager.service.useridentity.FinishCreatingUserIdentityReply;
import identitymanager.service.useridentity.FinishCreatingUserIdentityRequest;
import identitymanager.service.useridentity.FinishUpdatingUserIdentityReply;
import identitymanager.service.useridentity.FinishUpdatingUserIdentityRequest;
import identitymanager.service.useridentity.StartCreatingUserIdentityReply;
import identitymanager.service.useridentity.StartCreatingUserIdentityRequest;
import identitymanager.service.useridentity.StartUpdatingUserIdentityReply;
import identitymanager.service.useridentity.StartUpdatingUserIdentityRequest;
import identitymanager.service.useridentity.UpdateUserIdentityReply;
import identitymanager.service.useridentity.UpdateUserIdentityRequest;
import identitymanager.service.useridentity.UserIdentityExceptionKind;
import identitymanager.application.email.HtmlEmailContent;
import identitymanager.application.email.IEmailContent;
import identitymanager.application.email.IEmailContentGenerator;
import identitymanager.application.email.IEmailMessage;
import identitymanager.application.email.IEmailSender;
import identitymanager.application.email.IEmailSession;
import identitymanager.application.email.RecipientKind;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import strata1.common.logger.ILogger;
import strata1.persistence.repository.InsertFailedException;
import strata1.persistence.repository.InvalidInputException;
import strata1.persistence.repository.NotUniqueException;
import strata1.persistence.repository.UpdateFailedException;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class UserIdentityProcessor
    implements IUserIdentityProcessor
{
    private static final int EMAIL_VERIFICATION_MESSAGE = 1;
    
    private final IUserIdentityEventRepository itsEventRepository;
    private final IUserIdentityRepository      itsEntityRepository;
    private final IUserIdentityFactory         itsFactory;
    private final IEmailSession                itsEmailSession;
    private final IEmailContentGenerator       itsGenerator;
    private final EmailAddress                 itsFrom;
    private final String                       itsSubject;
    private final ILogger                      itsLogger;
    
    /************************************************************************
     * Creates a new UserIdentityProcessor. 
     *
     */
    @Inject
    public 
    UserIdentityProcessor(
        IUserIdentityEventRepository eventRepository,
        IUserIdentityRepository      entityRepository,
        IUserIdentityFactory         factory,
        IEmailSession                session,
        IEmailContentGenerator       generator,
        @Named("EmailVerificationFrom")
        EmailAddress                 from,
        @Named("EmailVerificationSubject")
        String                       subject,
        ILogger                      logger)
    {
        itsEventRepository  = eventRepository;
        itsEntityRepository = entityRepository;
        itsFactory          = factory;
        itsEmailSession     = session;
        itsGenerator        = generator;
        itsFrom             = from;
        itsSubject          = subject;
        itsLogger           = logger;
    }

    /************************************************************************
     * {@inheritDoc} 
     * @throws NotUniqueException 
     * @throws InvalidInputException 
     */
    @Override
    public StartCreatingUserIdentityReply 
    startCreatingUserIdentity(StartCreatingUserIdentityRequest request) 
        throws 
            InvalidInputException,
            NotUniqueException,
            InsertFailedException 
    {
        IUserIdentityEvent             event = null;
        StartCreatingUserIdentityReply reply = null;          
        
        itsLogger.logVerbose( 
            "Processing start creating useridentity request " + 
            request.getRequestId() );
        
        event = itsFactory.createUserIdentityEvent( request );
        
        if ( isUserNameAvailable( event ) )
            reply = doStartCreation( event,request );
        else
            reply = doFailUnavailable( event,request );
        
        return reply;
    }

    /************************************************************************
     * {@inheritDoc} 
     * @throws UpdateFailedException 
     */
    @Override
    public FinishCreatingUserIdentityReply 
    finishCreatingUserIdentity(FinishCreatingUserIdentityRequest request) 
        throws 
            InvalidInputException,
            NotUniqueException,
            InsertFailedException, 
            VerificationException, 
            UpdateFailedException
    {
        IUserIdentityEvent              event = null;
        FinishCreatingUserIdentityReply reply = null;          
        
        itsLogger.logVerbose( 
            "Processing finish creating useridentity request " + 
            request.getRequestId() );
        
        event = 
            itsEventRepository.getUserIdentityEvent( 
                request.getPendingEventId() );
        
        if ( isPending( event ) )
            reply = doFinishCreation( event,request );
        else
            reply = doFailExpired( event,request );
        
        return reply;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public StartUpdatingUserIdentityReply 
    startUpdatingUserIdentity(StartUpdatingUserIdentityRequest request) 
        throws 
            InvalidInputException,
            NotUniqueException,
            UpdateFailedException
    {
        return null;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public FinishUpdatingUserIdentityReply 
    finishUpdatingUserIdentity(FinishUpdatingUserIdentityRequest request) 
        throws 
            InvalidInputException,
            NotUniqueException,
            UpdateFailedException
    {
        return null;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public UpdateUserIdentityReply 
    updateUserIdentity(UpdateUserIdentityRequest request)
        throws InvalidInputException,NotUniqueException,UpdateFailedException
    {
        UpdateUserIdentityReply reply  = null;     
        IUserIdentity           user   = null;
        
        reply = itsFactory.createUserIdentityReply( request );
        
        if (!itsEntityRepository.hasUserIdentityWithUserName(request.getUserId()))
            user = itsEntityRepository.getUserIdentity( request.getUserId() );
        
        user = itsEntityRepository.updateUserIdentity( user );
        
        reply.setStatus( OperationStatus.SUCCEEDED );
        reply.setUser( itsFactory.createUserIdentityDescriptor( user ) );
        
        return reply;
    }

    /************************************************************************
     *  
     *
     * @param event
     * @return
     * @throws InvalidInputException
     * @throws NotUniqueException
     */
    private boolean 
    isUserNameAvailable(IUserIdentityEvent event) 
        throws InvalidInputException,NotUniqueException
    {
        return 
            !itsEntityRepository.hasUserIdentityWithUserName(
                event.getUserName());
    }

    /************************************************************************
     *  
     *
     * @param event
     * @param request
     * @return 
     * @throws InsertFailedException
     */
    private StartCreatingUserIdentityReply 
    doStartCreation(
        IUserIdentityEvent               event,
        StartCreatingUserIdentityRequest request) 
            throws InsertFailedException
    {
        IEmailSender                   sender     = null;
        IEmailMessage                  message    = null;
        Map<String,String>             parameters = null;
        StartCreatingUserIdentityReply reply      = null;
       
        sender     = itsEmailSession.createEmailSender();
        message    = itsEmailSession.createEmailMessage();
        parameters = new HashMap<String,String>();
        reply      = itsFactory.createUserIdentityReply( request );
        
        parameters.put( "userName",event.getUserName() );
        parameters.put( "verificationCode",event.getEmailVerification() );
        
        event = itsEventRepository.insertUserIdentityEvent( event );
                        
        message
            .insertRecipient( RecipientKind.TO,event.getEmailAddress() )
            .setFrom( itsFrom )
            .setSubject( itsSubject )
            .setContent( 
                itsGenerator.generate( 
                    EMAIL_VERIFICATION_MESSAGE,
                    parameters ) );
        
        sender.send( message );
        
        reply.setPendingEventId( event.getEventId() );
        reply.setStatus( OperationStatus.SUCCEEDED );
        
        return reply;
    }

    /************************************************************************
     *  
     *
     * @param event
     * @param request
     * @return 
     */
    private StartCreatingUserIdentityReply 
    doFailUnavailable(
        IUserIdentityEvent               event,
        StartCreatingUserIdentityRequest request)
    {
        StartCreatingUserIdentityReply reply = null;
        
        reply = itsFactory.createUserIdentityReply( request );
        
        reply.setStatus( OperationStatus.FAILED );
        reply.setExceptionKind( 
            UserIdentityExceptionKind.CREATE_FAILED );
        reply.setMessage( 
            "A user identity with user name: " + 
            event.getUserName() + 
            " already exists." );
        
        return reply;
    }

    /************************************************************************
     *  
     *
     * @param event
     * @return
     */
    private boolean 
    isPending(IUserIdentityEvent event)
    {
        return 
            event != null && 
            event.getStateKind() == UserIdentityStateKind.PENDING;
    }

    /************************************************************************
     *  
     *
     * @param event
     * @param request
     * @return
     * @throws VerificationException 
     * @throws UpdateFailedException 
     * @throws InsertFailedException 
     */
    private FinishCreatingUserIdentityReply 
    doFinishCreation(
        IUserIdentityEvent                event,
        FinishCreatingUserIdentityRequest request) 
            throws 
                VerificationException,
                UpdateFailedException, 
                InsertFailedException
    {
        IUserIdentity                   identity = null;
        FinishCreatingUserIdentityReply reply    = null;
        
        event.verifyEmailAddress( request.getVerificationCode() );
        event.setStateKind( UserIdentityStateKind.PROCESSED );
        
        identity = itsFactory.createUserIdentity( event );
        event    = itsEventRepository.updateUserIdentityEvent( event );
        identity = itsEntityRepository.insertUserIdentity( identity );
        reply    = itsFactory.createUserIdentityReply( request );
        
        reply.setStatus( OperationStatus.SUCCEEDED );
        reply.setUser( itsFactory.createUserIdentityDescriptor(identity) );
        return reply;
    }

    /************************************************************************
     *  
     *
     * @param event
     * @param request
     * @return
     */
    private FinishCreatingUserIdentityReply 
    doFailExpired(
        IUserIdentityEvent                event,
        FinishCreatingUserIdentityRequest request)
    {
        FinishCreatingUserIdentityReply reply  = null;
        String                          reason = null;
        
        reply = itsFactory.createUserIdentityReply( request );
        
        reply.setStatus( OperationStatus.FAILED );
        reply.setExceptionKind( 
            UserIdentityExceptionKind.CREATE_FAILED );
        
        switch ( event.getStateKind() )
        {
        case EXPIRED:
            reason = " has expired.";
            break;
            
        case CANCELLED:
            reason = " has been cancelled.";
            break;
            
        case PROCESSED:
            reason = " has been already processed.";
            break;
            
        default:
            throw 
                new IllegalStateException( 
                    "a pending state should not have executed this path" );
        }
            
        reply.setMessage( 
            "The <create user identity event>: " + 
            event.getEventId() + 
            " associated with <finish creating user identity request>: " + 
            request.getRequestId() + reason );
        
        return reply;
    }

}

// ##########################################################################
