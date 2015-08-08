// ##########################################################################
// # File Name:	HtmlEmailContentGenerator.java
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

import identitymanager.application.email.HtmlEmailContent;
import identitymanager.application.email.IEmailContent;
import identitymanager.application.email.IEmailContentGenerator;
import java.util.Map;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class HtmlEmailContentGenerator
    implements IEmailContentGenerator
{

    /************************************************************************
     * Creates a new HtmlEmailContentGenerator. 
     *
     */
    public 
    HtmlEmailContentGenerator() {}

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IEmailContent 
    generate(int messageId,Map<String,String> parameters)
    {
        String content = null;
        
        switch ( messageId )
        {
        case UserIdentityProcessor.EMAIL_VERIFICATION_MESSAGE:
            content = createEmailVerificationContent(parameters);
            break;
            
        default:
            content = "";
            break;
        }
        
        return new HtmlEmailContent( content );
    }

    /************************************************************************
     *  
     *
     * @param parameters
     * @return
     */
    private String 
    createEmailVerificationContent(Map<String,String> parameters)
    {
        StringBuilder builder = new StringBuilder();
        
        checkParameters( parameters,"userName","verificationCode" );

        builder
            .append( "<html>" )
            .append( "To complete user identity setup request for user name: " )
            .append( parameters.get( "userName" ) )
            .append( " please confirm email address with verification code: " )
            .append( parameters.get( "verificationCode" ) )
            .append( "." )
            .append( "<p/>" )
            .append( "<p/>" )
            .append(  "<a href=\"en.wikipedia.org\">Confirm Email Address</a>" )
            .append( "</html>" );
        
        return builder.toString();
    }

    /************************************************************************
     *  
     *
     * @param parameters
     * @param names
     */
    private void 
    checkParameters(Map<String,String> parameters,String... names)
    {
        for (String name:names)
            if ( !parameters.containsKey( name ) )
                throw 
                new IllegalArgumentException( 
                    "missing required parameter: " + name );
    }

}

// ##########################################################################
