// ##########################################################################
// # File Name:	EmailSenderTest.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerApplicationTest Framework.
// #
// #   			The IdentityManagerApplicationTest Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerApplicationTest Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerApplicationTest
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.application.email;

import static org.junit.Assert.*;
import identitymanager.common.value.EmailAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public abstract
class EmailSenderTest
{
    private IEmailSession itsSession;
    private IEmailSender  itsTarget;
    
    /************************************************************************
     *  
     *
     * @throws Exception
     */
    @Before
    public void 
    setUp() throws Exception
    {
        itsSession = createSession();
        itsTarget  = itsSession.createEmailSender();
    }

    /************************************************************************
     *  
     *
     * @throws java.lang.Exception
     */
    @After
    public void 
    tearDown() throws Exception
    {
        itsTarget  = null;
        itsSession = null;
    }

    /**
     * Test method for {@link IEmailSender#getSession()}.
     */
    @Test
    public void 
    testGetSession()
    {
        assertEquals( itsSession,itsTarget.getSession() );
    }

    /**
     * Test method for {@link IEmailSender#send(IEmailMessage)}.
     */
    @Test
    public void 
    testSend()
    {
        IEmailMessage    message  = itsSession.createEmailMessage();
        
        message
            .insertRecipient( RecipientKind.TO,new EmailAddress("johnliebenau@gmail.com") )
            .setFrom( new EmailAddress( "test@sapientiasystems.com" ) )
            .setSubject( "Test" )
            .setContent( 
                new HtmlEmailContent(
                "<html>text of <b>email message</b><p/><a href=\"https://en.wikipedia.org/\">click</a></html>") );
        
        itsTarget.send( message );
    }

    protected abstract IEmailSession 
    createSession();
    
}

// ##########################################################################
