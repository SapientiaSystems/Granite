// ##########################################################################
// # File Name:	UserIdentityAppModule.java
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

import identitymanager.service.useridentity.IUserIdentityService;
import identitymanager.application.email.IEmailContentGenerator;
import identitymanager.common.value.EmailAddress;
import strata1.injector.container.AbstractModule;
import strata1.injector.container.IContainer;
import strata1.injector.container.ThreadScope;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class UserIdentityAppModule
    extends AbstractModule
{

    /************************************************************************
     * Creates a new UserIdentityAppModule. 
     *
     * @param name
     */
    public 
    UserIdentityAppModule()
    {
        super( "UserIdentityAppModule" );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    initialize(IContainer container)
    {
        container
            .insertBinding( 
                bindType(IEmailContentGenerator.class)
                    .toType(HtmlEmailContentGenerator.class)
                    .withScope(new ThreadScope<IEmailContentGenerator>()))
            .insertBinding( 
                bindType(IUserIdentityFactory.class)
                    .toType(UserIdentityFactory.class)
                    .withScope(new ThreadScope<IUserIdentityFactory>()))
            .insertBinding( 
                bindType(EmailAddress.class)
                    .withKey("EmailVerificationFrom")
                    .toInstance(new EmailAddress("usersetup@zymurgistics.com")))
            .insertBinding( 
                bindType(String.class)
                    .withKey("EmailVerificationSubject")
                    .toInstance("Zymurgistics User Setup"))
            .insertBinding( 
                bindType(IUserIdentityProcessor.class)
                    .toType(UserIdentityProcessor.class)
                    .withScope(new ThreadScope<IUserIdentityProcessor>()))
            .insertBinding( 
                bindType(IUserIdentityService.class)
                    .withKey("UserIdentityServiceImp")
                    .toType(UserIdentityServiceImp.class)
                    .withScope(new ThreadScope<IUserIdentityService>()));
    }

}

// ##########################################################################
