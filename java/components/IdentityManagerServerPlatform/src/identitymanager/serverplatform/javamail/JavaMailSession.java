// ##########################################################################
// # File Name:	EmailSession.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerServerPlatform Framework.
// #
// #   			The IdentityManagerServerPlatform Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerServerPlatform Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerServerPlatform
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.serverplatform.javamail;

import identitymanager.application.email.IEmailMessage;
import identitymanager.application.email.IEmailReceiver;
import identitymanager.application.email.IEmailSender;
import identitymanager.application.email.IEmailSession;
import javax.inject.Inject;
import javax.mail.Session;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class JavaMailSession
    implements IEmailSession
{
    private final Session itsImp;
    
    /************************************************************************
     * Creates a new EmailSession. 
     *
     */
    @Inject
    public 
    JavaMailSession(Session imp)
    {
        itsImp = imp;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IEmailMessage 
    createEmailMessage()
    {
        return new JavaMailMessage( this );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IEmailSender 
    createEmailSender()
    {
        return new JavaMailSender( this );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IEmailReceiver 
    createEmailReceiver()
    {
        return null;
    }

    /************************************************************************
     *  
     *
     * @return
     */
    public Session
    getImp()
    {
        return itsImp;
    }
}

// ##########################################################################
