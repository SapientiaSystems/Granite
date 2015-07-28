// ##########################################################################
// # File Name:	DefaultIdentifierGenerator.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerCommon Framework.
// #
// #   			The IdentityManagerCommon Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerCommon Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerCommon
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.common.utility;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class DefaultIdentifierGenerator
    implements IIdentifierGenerator
{
    private final Random itsSource;
    
    /************************************************************************
     * Creates a new DefaultIdentifierGenerator. 
     *
     */
    public 
    DefaultIdentifierGenerator()
    {
        itsSource = new SecureRandom();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Integer 
    getNextIntegerId()
    {
        int id = itsSource.nextInt();
        
        return id >= 0 ? id : -id;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Integer 
    getNextIntegerId(int upperBound)
    {
        return itsSource.nextInt(upperBound);
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Long 
    getNextLongId()
    {
        long id = itsSource.nextLong();
        
        return id >= 0 ? id : -id; 
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Long 
    getNextLongId(long upperBound)
    {
        long id = itsSource.nextLong();
        
        id = id >= 0 ? id : -id;
        return id % upperBound;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String 
    getNextStringId()
    {
        return UUID.randomUUID().toString();
    }

}

// ##########################################################################
