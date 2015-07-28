// ##########################################################################
// # File Name:	EmailSender.java
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
import identitymanager.application.email.IEmailSender;
import identitymanager.application.email.IEmailSession;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class JavaMailSender
    implements IEmailSender
{
    private final JavaMailSession itsSession;
    
    /************************************************************************
     * Creates a new EmailSender. 
     *
     * @param session
     */
    public 
    JavaMailSender(JavaMailSession session)
    {
        itsSession = session;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IEmailSession 
    getSession()
    {
        return itsSession;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IEmailSender 
    send(IEmailMessage message)
    {
        Transport   transport = null;
        
        try
        {
            Session     session   = itsSession.getImp();
            MimeMessage msg       = ((JavaMailMessage)message).getImp();
            String      host      = session.getProperty( "mail.smtp.host" );
            String      user      = session.getProperty( "mail.smtp.user" );
            String      password  = session.getProperty( "mail.smtp.password" );
            
            transport = session.getTransport("smtp");
            msg.saveChanges();
            
            transport.connect( host,user,password );
            transport.sendMessage( msg,msg.getAllRecipients());
            return this;
        }
        catch(MessagingException e)
        {
            throw new IllegalStateException( e );
        }
        finally
        {
            if ( transport != null ) try
            {
                transport.close();
            }
            catch(MessagingException e)
            {
                throw new IllegalStateException( e );
            }
        }
    }

}

// ##########################################################################
