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

import identitymanager.application.email.EmailAppModule;
import identitymanager.application.email.EmailSenderTest;
import identitymanager.application.email.IEmailSession;
import strata1.injector.container.Container;
import strata1.injector.container.IContainer;
import strata1.injector.property.PropertiesModule;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class JavaMailSenderTest
    extends EmailSenderTest
{

    @Override
    protected IEmailSession 
    createSession()
    {
        IContainer container = new Container();
        
        new PropertiesModule(createProperties()).initialize(container);
        new JavaMailModule().initialize(container);
        return container.getInstance( IEmailSession.class );
    }
    
    /************************************************************************
     *  
     *
     * @return
     */
    private Properties
    createProperties()
    {
        Properties  properties = new Properties();
        InputStream input = 
            this
                .getClass()
                .getResourceAsStream( 
                    "/identitymanager/application/email/emailtest.properties" );
        
        try
        {
            properties.load( input );
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
        
        return properties;
    }

}

// ##########################################################################
