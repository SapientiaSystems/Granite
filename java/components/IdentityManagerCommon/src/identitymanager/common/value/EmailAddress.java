// ##########################################################################
// # File Name:	EmailAddress.java
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

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import strata1.common.utility.ICopyable;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class EmailAddress
    implements ICopyable,Comparable<EmailAddress>,Serializable
{
    private static final long serialVersionUID = 2320238746080434459L;
    private final String itsEmail;
    
    /************************************************************************
     * Creates a new EmailAddress. 
     *
     * @param email
     */
    public 
    EmailAddress(String email)
    {
        validate( email );
        itsEmail = email;
    }

    /************************************************************************
     * Creates a new EmailAddress. 
     *
     * @param other
     */
    public 
    EmailAddress(EmailAddress other)
    {
        this( other.itsEmail );
    }
    
    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public EmailAddress 
    copy()
    {
        return new EmailAddress( this );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public int 
    compareTo(EmailAddress other)
    {
        return itsEmail.compareToIgnoreCase( other.itsEmail );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    equals(Object other)
    {
        if ( other instanceof EmailAddress )
            return itsEmail.equalsIgnoreCase( ((EmailAddress)other).itsEmail );
        
        return false;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public int 
    hashCode()
    {
        return 31 * itsEmail.hashCode();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String 
    toString()
    {
        return itsEmail;
    }

    /************************************************************************
     *  
     *
     * @param addresses
     * @return
     */
    public static List<EmailAddress>
    createList(String addresses)
    {
        List<EmailAddress> output    = new ArrayList<EmailAddress>();
        StringTokenizer    tokenizer = new StringTokenizer( addresses,"," );
        
        while ( tokenizer.hasMoreTokens() )
            output.add( new EmailAddress( tokenizer.nextToken() ) );
        
        return output;
    }
    
    /************************************************************************
     *  
     *
     * @param email
     * @throws IllegalArgumentException
     */
    private void
    validate(String email) 
        throws IllegalArgumentException 
    {
        try
        {
            new InternetAddress(email).validate();
        }
        catch(AddressException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
    
}

// ##########################################################################
