// ##########################################################################
// # File Name:	Permission.java
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

package identitymanager.service.subject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import strata1.common.utility.Triple;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class ResourceActionPermission
    implements IPermission
{

    private static final long   serialVersionUID = 4719221348231785474L;
    
    private static final String STAR = "*";
    private static final String PRIMARY_SEPARATOR = ":";
    private static final String SECONDARY_SEPARATOR = ",";
    
    private static final String ALL = 
        STAR + PRIMARY_SEPARATOR + STAR + PRIMARY_SEPARATOR + STAR;
    
    private final Set<String>   itsTypes;
    private final Set<String>   itsInstances;
    private final Set<String>   itsActions;
    private final String        itsName;

    /************************************************************************
     * Creates a new Permission. 
     *
     * @param name
     */
    public 
    ResourceActionPermission(String specification)
    {
        Triple<Set<String>,Set<String>,Set<String>> tokens = 
            tokenize(specification);
        
        itsTypes     = tokens.getFirst();
        itsInstances = tokens.getSecond();
        itsActions   = tokens.getThird();
        itsName      = initializeName();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    equals(Object other)
    {
        if ( this == other )
            return true;
        
        if ( other instanceof ResourceActionPermission)
        {
            ResourceActionPermission p = (ResourceActionPermission)other;
            
            return
                itsTypes.equals( p.itsTypes ) &&
                itsInstances.equals( p.itsInstances ) &&
                itsActions.equals( p.itsActions );
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
        int hash = 37;
        
        hash = hash * 23 + itsTypes.hashCode();
        hash = hash * 29 + itsInstances.hashCode();
        hash = hash * 31 + itsActions.hashCode();
        
        return hash; 
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String 
    toString()
    {
        return getName();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String 
    getName()
    {
        return itsName;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    implies(IPermission other)
    {
        ResourceActionPermission p = null;
        
        if ( ALL.equals( getName() ) )
            return true;
        
        p = (ResourceActionPermission)other;
        
        if ( !match(itsTypes,p.itsTypes))
            return false;
        
        if ( !match(itsInstances,p.itsInstances))
            return false;
        
        if ( !match(itsActions,p.itsActions))
            return false;
        
        return true;
    }

    /************************************************************************
     *  
     *
     * @param specification
     * @return
     */
    private Triple<Set<String>,Set<String>,Set<String>> 
    tokenize(String specification)
    {
        List<String> primaryTokens = 
            splitIntoList( specification,PRIMARY_SEPARATOR );
        
        if ( primaryTokens.size() != 3 )
            throw new IllegalArgumentException("specification");        
        
        return Triple.create( 
            splitIntoSet(primaryTokens.get( 0 ),SECONDARY_SEPARATOR),
            splitIntoSet(primaryTokens.get( 1 ),SECONDARY_SEPARATOR),
            splitIntoSet(primaryTokens.get( 2 ),SECONDARY_SEPARATOR) );
    }

    /************************************************************************
     *  
     *
     * @param separator
     * @return
     */
    private List<String> 
    splitIntoList(String source,String separator)
    {
        List<String> output = new ArrayList<String>();
        
        for (String token : source.split( separator ))
            output.add( token.trim() );
        
        return output;
    }

    /************************************************************************
     *  
     *
     * @param separator
     * @return
     */
    private Set<String> 
    splitIntoSet(String source,String separator)
    {
        Set<String> output = new TreeSet<String>();
        
        for (String token : source.split( separator ))
            output.add( token.trim() );
        
        return output;
    }
     
    /************************************************************************
     *  
     *
     * @param source
     * @param separator
     * @return
     */
    private String
    mergeIntoString(Set<String> source,String separator)
    {
        StringBuilder    builder  = new StringBuilder();
        Iterator<String> iterator = source.iterator();
        
        if ( iterator.hasNext() )
            builder.append( iterator.next() );
        
        while( iterator.hasNext() )
            builder
                .append( separator )
                .append( iterator.next() );
        
        return builder.toString();
    }
    
    /************************************************************************
     *  
     *
     * @return
     */
    private String
    initializeName()
    {
        StringBuilder builder = new StringBuilder();
        
        builder
            .append(mergeIntoString(itsTypes,SECONDARY_SEPARATOR))
            .append( PRIMARY_SEPARATOR )
            .append(mergeIntoString(itsInstances,SECONDARY_SEPARATOR))
            .append( PRIMARY_SEPARATOR )
            .append(mergeIntoString(itsActions,SECONDARY_SEPARATOR));
        
        return builder.toString();        
    }
    
    /************************************************************************
     *  
     *
     * @param x
     * @param y
     * @return
     */
    private boolean
    match(Set<String> x,Set<String> y)
    {
        if ( x.contains( STAR ) )
            return true;
        
        return x.containsAll( y );
    }

}

// ##########################################################################
