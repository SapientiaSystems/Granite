// ##########################################################################
// # File Name:	VerificationException.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerDomain Framework.
// #
// #   			The IdentityManagerDomain Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerDomain Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerDomain
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.domain.useridentity;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class VerificationException
    extends Exception
{

    private static final long serialVersionUID = -3423360670223511241L;

    /************************************************************************
     * Creates a new VerificationException. 
     *
     * @param message
     */
    public 
    VerificationException(String message)
    {
        super( message );
    }
}

// ##########################################################################
