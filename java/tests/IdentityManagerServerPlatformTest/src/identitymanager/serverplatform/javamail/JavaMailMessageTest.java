// ##########################################################################
// # File Name:	JavaMailMessageTest.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerServerPlatformTest Framework.
// #
// #   			The IdentityManagerServerPlatformTest Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerServerPlatformTest Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerServerPlatformTest
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.serverplatform.javamail;

import static org.junit.Assert.*;
import identitymanager.application.email.EmailMessageTest;
import identitymanager.application.email.IEmailSession;
import org.junit.Test;
import javax.mail.Session;
import java.util.Properties;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class JavaMailMessageTest
    extends EmailMessageTest
{

    @Override
    protected IEmailSession 
    createSession()
    {
        Properties properties = new Properties();
        Session    session    = null;
        
        properties.put("mail.smtp.host","smtp.gmail.com");
        properties.put("mail.smtp.port", "587");        
        properties.put("mail.smtps.auth","true");
        properties.put("mail.smtp.starttls.enable","true");
        properties.put("mail.smtp.user","johnliebenau@gmail.com");        
        properties.put("mail.smtp.password","PiHUB(9D@i:N");        
        session = Session.getInstance( properties );
        
        return new JavaMailSession(session);
    }

}

// ##########################################################################
