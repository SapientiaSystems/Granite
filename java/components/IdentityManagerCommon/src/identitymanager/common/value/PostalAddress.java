// ##########################################################################
// # File Name:	PostalAddress.java
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
import java.io.Serializable;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class PostalAddress
    implements ICopyable,Comparable<PostalAddress>,Serializable
{
    private static final long serialVersionUID = 8284898731222924307L;
    
    private final String itsAddress;
    private final String itsStreet;
    private final String itsCity;
    private final String itsStateProvince;
    private final String itsCountryCode;
    private final String itsPostalCode;
    
    /************************************************************************
     * Creates a new PostalAddress. 
     *
     */
    public 
    PostalAddress(
        final String address,
        final String street,
        final String city,
        final String stateProvince,
        final String countryCode,
        final String postalCode)
    {
        itsAddress       = address;
        itsStreet        = street;
        itsCity          = city;
        itsStateProvince = stateProvince;
        itsCountryCode   = countryCode;
        itsPostalCode    = postalCode;
    }

    /************************************************************************
     * Creates a new PostalAddress. 
     *
     * @param other
     */
    public
    PostalAddress(PostalAddress other)
    {
        this( 
            other.itsAddress,
            other.itsStreet,
            other.itsCity,
            other.itsStateProvince,
            other.itsCountryCode,
            other.itsPostalCode );
    }
    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public PostalAddress 
    copy()
    {
        return new PostalAddress( this );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public int 
    compareTo(PostalAddress other)
    {
        int result = 
            itsPostalCode.compareToIgnoreCase( other.itsPostalCode );
        
        if ( result != 0 )
            return result;
        
        result = 
            itsCountryCode.compareToIgnoreCase( other.itsCountryCode );
        
        if ( result != 0 )
            return result;
        
        result = 
            itsStateProvince.compareToIgnoreCase( other.itsStateProvince );
        
        if ( result != 0 )
            return result;
        
        result = 
            itsCity.compareToIgnoreCase( other.itsCity );
        
        if ( result != 0 )
            return result;
        
        result =
            itsStreet.compareToIgnoreCase( other.itsStreet );
        
        if ( result != 0 )
            return result;
        
        result = 
            itsAddress.compareToIgnoreCase( other.itsAddress );
        
        return result;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    equals(Object other)
    {
        if ( other instanceof PostalAddress )
            return compareTo( (PostalAddress)other ) == 0;
        
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
        
        hash = 31 * hash + itsAddress.hashCode();
        hash = 31 * hash + itsStreet.hashCode();
        hash = 31 * hash + itsCity.hashCode();
        hash = 31 * hash + itsStateProvince.hashCode();
        hash = 31 * hash + itsCountryCode.hashCode();
        hash = 31 * hash + itsPostalCode.hashCode();
        
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
            .append(  itsAddress )
            .append( ' ' )
            .append( itsStreet )
            .append(  '\n' )
            .append(  itsCity )
            .append( ' ' )
            .append( itsStateProvince )
            .append( ' ' )
            .append( itsCountryCode )
            .append(  ' ' )
            .append( itsPostalCode );
        
        return builder.toString();
    }

    /************************************************************************
     *  
     *
     * @return
     */
    public String
    getAddress()
    {
        return itsAddress;
    }
    
    /************************************************************************
     *  
     *
     * @return
     */
    public String
    getStreet()
    {
        return itsStreet;
    }
    
    /************************************************************************
     *  
     *
     * @return
     */
    public String
    getCity()
    {
        return itsCity;
    }
    
    /************************************************************************
     *  
     *
     * @return
     */
    public String
    getStateProvince()
    {
        return itsStateProvince;
    }
    
    /************************************************************************
     *  
     *
     * @return
     */
    public String
    getCountryCode()
    {
        return itsCountryCode;
    }
    
    /************************************************************************
     *  
     *
     * @return
     */
    public String
    getPostalCode()
    {
        return itsPostalCode;
    }
}

// ##########################################################################
