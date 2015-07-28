// ##########################################################################
// # File Name:	PhoneNumber.java
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

package identitymanager.common.value;

import strata1.common.utility.ICopyable;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class PhoneNumber
    implements ICopyable,Comparable<PhoneNumber>
{
    private final String itsCountryCode;
    private final String itsAreaCode;
    private final String itsNumber;

    /************************************************************************
     * Creates a new PhoneNumber. 
     *
     * @param countryCode
     * @param areaCode
     * @param number
     */
    public 
    PhoneNumber(
        final String countryCode,
        final String areaCode,
        final String number)
    {
        itsCountryCode = countryCode;
        itsAreaCode    = areaCode;
        itsNumber      = number;
    }

    /************************************************************************
     * Creates a new PhoneNumber. 
     *
     * @param other
     */
    public 
    PhoneNumber(PhoneNumber other)
    {
        this( other.itsCountryCode,other.itsAreaCode,other.itsNumber );
    }
    
    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public ICopyable 
    copy()
    {
        return new PhoneNumber( this );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public int 
    compareTo(PhoneNumber other)
    {
        int result = 
            itsCountryCode.compareToIgnoreCase( other.itsCountryCode );
        
        if ( result != 0 )
            return result;
        
        result = 
            itsAreaCode.compareToIgnoreCase( other.itsAreaCode );
        
        if ( result != 0 )
            return result;
        
        result = 
            itsNumber.compareToIgnoreCase( other.itsNumber );
        
        return result;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    equals(Object other)
    {
        if ( other instanceof PhoneNumber )
            return compareTo( (PhoneNumber)other ) == 0;
        
        return false;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public int 
    hashCode()
    {
        int hash = 31;
        
        hash = 31 * hash + itsCountryCode.hashCode();
        hash = 31 * hash + itsAreaCode.hashCode();
        hash = 31 * hash + itsNumber.hashCode();
        
        return hash;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String 
    toString()
    {
        StringBuilder builder = new StringBuilder();
        
        builder
            .append( itsCountryCode ).append( '-' )
            .append(  itsAreaCode ).append(  '-' )
            .append( itsNumber);
        
        return builder.toString();
    }

}

// ##########################################################################
