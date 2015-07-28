// ##########################################################################
// # File Name:	CreateUserIdentityFailedException.java
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

package identitymanager.service.useridentity;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class CreateUserIdentityFailedException
    extends UserIdentityException
{

    private static final long serialVersionUID = 255592218841780391L;

    /************************************************************************
     * Creates a new CreateUserIdentityFailedException. 
     *
     * @param request
     */
    public 
    CreateUserIdentityFailedException(UserIdentityRequest request)
    {
        super( request );
    }

    /************************************************************************
     * Creates a new CreateUserIdentityFailedException. 
     *
     * @param request
     * @param message
     */
    public 
    CreateUserIdentityFailedException(
        UserIdentityRequest request,
        String              message)
    {
        super( request,message );
    }

    /************************************************************************
     * Creates a new CreateUserIdentityFailedException. 
     *
     * @param request
     * @param cause
     */
    public 
    CreateUserIdentityFailedException(
        UserIdentityRequest request,
        Throwable           cause)
    {
        super( request,cause );
    }

    /************************************************************************
     * Creates a new CreateUserIdentityFailedException. 
     *
     * @param request
     * @param message
     * @param cause
     */
    public 
    CreateUserIdentityFailedException(
        UserIdentityRequest request,
        String              message,
        Throwable           cause)
    {
        super( request,message,cause );
    }

}

// ##########################################################################
