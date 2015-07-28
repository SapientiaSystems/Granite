// ##########################################################################
// # File Name:	EmailMessageTest.java
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
import java.util.List;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public abstract
class EmailMessageTest
{
    private IEmailSession itsSession;
    private IEmailMessage itsTarget;
    
    /************************************************************************
     *  
     *
     * @throws Exception
     */
    @Before
    public void 
    setUp() 
        throws Exception
    {
        itsSession = createSession();
        itsTarget  = itsSession.createEmailMessage();
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
        itsTarget = null;
        itsSession = null;
    }

    /**
     * Test method for {@link IEmailMessage#insertRecipient(RecipientKind, EmailAddress)}.
     */
    @Test
    public void 
    testInsertRecipient()
    {
        EmailAddress expected = new EmailAddress("johnliebenau@gmail.com");
        List<EmailAddress> actual   = null;
        
        itsTarget.insertRecipient( RecipientKind.TO, expected );
        actual = itsTarget.getRecipients( RecipientKind.TO );
        
        assertEquals( 1,actual.size() );
        assertTrue( actual.contains( expected ) );   
    }

    /**
     * Test method for {@link IEmailMessage#setFrom(EmailAddress)}.
     */
    @Test
    public void 
    testSetGetFrom()
    {
        EmailAddress expected = new EmailAddress("johnliebenau@gmail.com");
        EmailAddress actual   = null;
        
        itsTarget.setFrom( expected );
        actual = itsTarget.getFrom();
        
        assertEquals( expected,actual );   
    }

    /**
     * Test method for {@link IEmailMessage#setSubject(String)}.
     */
    @Test
    public void 
    testSetGetSubject()
    {
        String expected = "test subject";
        String actual   = null;
        
        itsTarget.setSubject( expected );
        actual = itsTarget.getSubject();
        
        assertEquals( expected,actual );
    }

    /**
     * Test method for {@link IEmailMessage#setContent(IEmailContent)}.
     */
    @Test
    public void 
    testSetGetContent1()
    {
        TextEmailContent expected = new TextEmailContent("text of email message");
        IEmailContent    actual   = null;
        
        itsTarget.setContent( expected );
        actual = itsTarget.getContent();
        
        assertEquals( expected,actual );
    }

    /**
     * Test method for {@link IEmailMessage#setContent(IEmailContent)}.
     */
    @Test
    public void 
    testSetGetContent2()
    {
        HtmlEmailContent expected = new HtmlEmailContent("<html>text of <b>email message</b></html>");
        IEmailContent    actual   = null;
        
        itsTarget.setContent( expected );
        actual = itsTarget.getContent();
        
        assertEquals( expected,actual );
    }


    /**
     * Test method for {@link IEmailMessage#getSession()}.
     */
    @Test
    public void 
    testGetSession()
    {
        assertEquals( itsSession,itsTarget.getSession() );
    }

    protected abstract IEmailSession 
    createSession();

}

// ##########################################################################
