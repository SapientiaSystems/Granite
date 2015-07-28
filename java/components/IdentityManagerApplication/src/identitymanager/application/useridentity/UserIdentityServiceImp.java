// ##########################################################################
// # File Name:	UserIdentityServiceImp.java
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

import identitymanager.service.useridentity.CreateUserIdentityFailedException;
import identitymanager.service.useridentity.FinishCreatingUserIdentityReply;
import identitymanager.service.useridentity.FinishCreatingUserIdentityRequest;
import identitymanager.service.useridentity.FinishUpdatingUserIdentityRequest;
import identitymanager.service.useridentity.IUserIdentityReplyReceiver;
import identitymanager.service.useridentity.IUserIdentityService;
import identitymanager.service.useridentity.StartCreatingUserIdentityReply;
import identitymanager.service.useridentity.StartCreatingUserIdentityRequest;
import identitymanager.service.useridentity.StartListeningRequest;
import identitymanager.service.useridentity.StartUpdatingUserIdentityRequest;
import identitymanager.service.useridentity.StopListeningRequest;
import identitymanager.service.useridentity.UpdateUserIdentityRequest;
import identitymanager.service.useridentity.UserIdentityException;
import identitymanager.service.useridentity.UserIdentityRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import strata1.common.logger.ILogger;
import strata1.persistence.repository.IRepositoryContext;
import strata1.persistence.repository.IUnitOfWork;
import strata1.persistence.repository.RepositoryException;
import strata1.persistence.repository.RollbackFailedException;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class UserIdentityServiceImp
    implements IUserIdentityService
{
    private final IRepositoryContext     itsContext;
    private final IUserIdentityProcessor itsProcessor;
    private final ILogger                itsLogger;
    
    /************************************************************************
     * Creates a new UserIdentityServiceImp. 
     *
     */
    public 
    UserIdentityServiceImp(
        IRepositoryContext     context,
        IUserIdentityProcessor processor,
        ILogger                logger)
    {
        itsContext   = context;
        itsProcessor = processor;
        itsLogger    = logger;

    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    startCreatingUserIdentity(
        IUserIdentityReplyReceiver        receiver,
        StartCreatingUserIdentityRequest  request)
    {
        IUnitOfWork unitOfWork = itsContext.getUnitOfWork();
        
        try
        {
            StartCreatingUserIdentityReply reply = 
                itsProcessor.startCreatingUserIdentity( request );
            
            itsLogger.logDebug( "Committing start creating useridentity request." );
            unitOfWork.commit();
            itsLogger.logDebug( "Committed start creating useridentity request." );
            receiver.onStartCreatingUserIdentity( reply );
        }
        catch (RepositoryException e)
        {
            notifyCreateUserIdentityFailedException( receiver,request,e );           
            rollback( unitOfWork );
        }
        catch (Throwable t)
        {
            notifyException( receiver,t );
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
        IUnitOfWork unitOfWork = itsContext.getUnitOfWork();
        
        try
        {
            FinishCreatingUserIdentityReply reply = 
                itsProcessor.finishCreatingUserIdentity( request );
            
            itsLogger.logDebug( "Committing finish creating useridentity request." );
            unitOfWork.commit();
            itsLogger.logDebug( "Committed finish creating useridentity request." );
            receiver.onFinishCreatingUserIdentity( reply );
        }
        catch (RepositoryException e)
        {
            notifyCreateUserIdentityFailedException( receiver,request,e );           
            rollback( unitOfWork );
        }
        catch (Throwable t)
        {
            notifyException( receiver,t );
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
     *  
     *
     * @param unitOfWork
     */
    private void 
    rollback(IUnitOfWork unitOfWork)
    {
        try
        {
            unitOfWork.rollback();
        }
        catch (RollbackFailedException e2)
        {
            itsLogger.logError( e2.getMessage() );
        }
    }

    /************************************************************************
     *  
     *
     * @param receiver
     * @param request
     * @param e1
     */
    private void 
    notifyCreateUserIdentityFailedException(
        IUserIdentityReplyReceiver receiver,
        UserIdentityRequest        request,
        Exception                  exception)
    {
        itsLogger.logError( exception.getMessage() );
        
        receiver.onUserIdentityException( 
            new CreateUserIdentityFailedException( 
                request,
                exception.getMessage() ) );
    }

    /************************************************************************
     *  
     *
     * @param receiver
     * @param t
     */
    private void 
    notifyException(IUserIdentityReplyReceiver receiver,Throwable t)
    {
        StringWriter writer = new StringWriter();
        PrintWriter  output = new PrintWriter( writer );
        
        t.printStackTrace( output );
        itsLogger.logError( writer.toString() );
        receiver.onException( new Exception(t.getMessage()) );
    }

}

// ##########################################################################
