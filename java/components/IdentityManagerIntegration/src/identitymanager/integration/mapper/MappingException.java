// ##########################################################################
// # File Name:	MappingException.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerIntegration Framework.
// #
// #   			The IdentityManagerIntegration Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerIntegration Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerIntegration
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.integration.mapper;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class MappingException
    extends Exception
{

    private static final long serialVersionUID = -7910732514755524059L;

    /************************************************************************
     * Creates a new MappingException. 
     *
     */
    public 
    MappingException()
    {
    }

    /************************************************************************
     * Creates a new MappingException. 
     *
     * @param message
     */
    public 
    MappingException(String message)
    {
        super( message );
    }

    /************************************************************************
     * Creates a new MappingException. 
     *
     * @param cause
     */
    public 
    MappingException(Throwable cause)
    {
        super( cause );
    }

    /************************************************************************
     * Creates a new MappingException. 
     *
     * @param message
     * @param cause
     */
    public 
    MappingException(String message,Throwable cause)
    {
        super( message,cause );
    }

}

// ##########################################################################
