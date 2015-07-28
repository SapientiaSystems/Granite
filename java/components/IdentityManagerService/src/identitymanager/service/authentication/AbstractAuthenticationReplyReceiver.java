// ##########################################################################
// # File Name:	AbstractAuthenticationReplyReceiver.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerService Framework.
// #
// #   			The IdentityManagerService Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerService Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerService
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.service.authentication;

import identitymanager.service.subject.ISubject;
import identitymanager.service.shared.OperationStatus;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public abstract 
class AbstractAuthenticationReplyReceiver
    implements IAuthenticationReplyReceiver
{
    private ISubject        itsSubject;
    private ISubjectFactory itsFactory;
    
    /************************************************************************
     * Creates a new AbstractAuthenticationReplyReceiver. 
     *
     */
    public 
    AbstractAuthenticationReplyReceiver()
    {
        itsSubject = null;
        itsFactory = new SubjectFactory();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onAuthenticate(AuthenticateReply reply)
    {
        if ( isAuthenticated(reply) )
            onSuccess( toSubject( reply ) );
        else
            onFailure( reply.getMessage() );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onException(Exception exception)
    {
        itsSubject = null;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public ISubject 
    getSubject()
    {
        return itsSubject;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    hasSubject()
    {
        return itsSubject != null;
    }

    /************************************************************************
     *  
     *
     * @param subject
     */
    protected abstract void
    onSuccess(ISubject subject);
    
    /************************************************************************
     *  
     *
     * @param reason
     */
    protected abstract void
    onFailure(String reason);

    /************************************************************************
     *  
     *
     * @param subject
     */
    protected void
    setSubject(ISubject subject)
    {
        itsSubject = subject;
    }
    
    /************************************************************************
     *  
     *
     * @param reply
     * @return
     */
    private boolean 
    isAuthenticated(AuthenticateReply reply)
    {
        return reply.getStatus().equals( OperationStatus.SUCCEEDED );
    }

    /************************************************************************
     *  
     *
     * @param reply
     * @return
     */
    private ISubject 
    toSubject(AuthenticateReply reply)
    {
        SubjectDescriptor subject = reply.getSubject();
        
        if ( subject instanceof UserDescriptor )
            return itsFactory.createUser( (UserDescriptor)subject );
        
        return null;
    }
}

// ##########################################################################
