// ##########################################################################
// # File Name:	PersonName.java
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
class PersonName
    implements ICopyable,Comparable<PersonName>
{
    private final String itsFirstName;
    private final String itsMiddleName;
    private final String itsLastName;
    
    /************************************************************************
     * Creates a new PersonName. 
     *
     * @param first
     * @param middle
     * @param last
     */
    public 
    PersonName(String first,String middle,String last)
    {
        itsFirstName  = first;
        itsMiddleName = middle;
        itsLastName   = last;
    }

    /************************************************************************
     * Creates a new PersonName. 
     *
     * @param first
     * @param last
     */
    public 
    PersonName(String first,String last)
    {
        this( first,"",last );
    }
    
    /************************************************************************
     * Creates a new PersonName. 
     *
     * @param other
     */
    public 
    PersonName(PersonName other)
    {
        this( 
            other.getFirstName(),
            other.getMiddleName(),
            other.getLastName() );
    }
    
    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public ICopyable 
    copy()
    {
        return new PersonName( this );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public int 
    compareTo(PersonName other)
    {
        int result = 
            getLastName().compareToIgnoreCase( other.getLastName() );
        
        if ( result != 0 )
            return result;
        
        result = 
            getFirstName().compareToIgnoreCase( other.getFirstName() );
        
        if ( result != 0 )
            return result;
        
        result = 
            getMiddleName().compareToIgnoreCase( other.getMiddleName() );
        
        return result;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    equals(Object other)
    {
        if ( other instanceof PersonName)
        {
            PersonName p = (PersonName)other;
            
            return 
                getFirstName().equalsIgnoreCase( p.getFirstName() ) &&
                getMiddleName().equalsIgnoreCase( p.getMiddleName() ) &&
                getLastName().equalsIgnoreCase( p.getLastName() );
        }
        
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
        
        hash = 31 * hash + itsFirstName.hashCode();
        hash = 31 * hash + itsMiddleName.hashCode();
        hash = 31 * hash + itsLastName.hashCode();
        
        return hash; 
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String 
    toString()
    {
        StringBuilder output = new StringBuilder();
        
        output.append( getFirstName() );
        
        if ( hasMiddleName() )
            output.append( " " ).append( getMiddleName() );
        
        if ( hasLastName() )
            output.append( " " ).append( getLastName() );
        
        
        return output.toString().trim();
    }

    /************************************************************************
     *  
     *
     * @return
     */
    public String 
    getFirstName()
    {
        return itsFirstName;
    }
    
    /************************************************************************
     *  
     *
     * @return
     */
    public String
    getMiddleName()
    {
        return itsMiddleName;
    }
    
    /************************************************************************
     *  
     *
     * @return
     */
    public String
    getLastName()
    {
        return itsLastName;
    }
    
    /************************************************************************
     *  
     *
     * @return
     */
    public boolean
    hasFirstName()
    {
        return 
            itsFirstName != null &&
            !itsFirstName.isEmpty();
    }
    
    /************************************************************************
     *  
     *
     * @return
     */
    public boolean
    hasMiddleName()
    {
        return 
            itsMiddleName != null &&
            !itsMiddleName.isEmpty();
    }
    
    /************************************************************************
     *  
     *
     * @return
     */
    public boolean
    hasLastName()
    {
        return 
            itsLastName != null &&
            !itsLastName.isEmpty();
    }
}

// ##########################################################################
