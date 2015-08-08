// ##########################################################################
// # File Name:	MoneyType.java
// #
// # Copyright:	2012, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the StrataEntity Framework.
// #
// #   			The StrataEntity Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The StrataEntity Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the StrataEntity
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.serverplatform.hibernatepersistence;

import identitymanager.common.value.PostalAddress;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class PostalAddressType 
    implements CompositeUserType
{
    /************************************************************************
     * Creates a new {@code DateTimeType}. 
     *
     */
    public
    PostalAddressType() {}
    
    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String[] 
    getPropertyNames()
    {
        return 
            new String[] {
                "Address",
                "Street",
                "City",
                "StateProvince",
                "CountryCode",
                "PostalCode" };
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Type[] 
    getPropertyTypes()
    {
        return 
            new Type[] {
                new StringType(),
                new StringType(),
                new StringType(),
                new StringType(),
                new StringType(),
                new StringType() };
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Object 
    getPropertyValue(Object component,int property)
        throws HibernateException
    {
        PostalAddress post = (PostalAddress)component;

        if ( property == 0 ) return post.getAddress();

        if ( property == 1 ) return post.getStreet();

        if ( property == 2 ) return post.getCity();

        if ( property == 3 ) return post.getStateProvince();

        if ( property == 4 ) return post.getCountryCode();

        if ( property == 5 ) return post.getPostalCode();
        
        throw new HibernateException("unknown property");
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    setPropertyValue(Object component,int property,Object value)
        throws HibernateException
    {
        throw new HibernateException("PostalAddress is immutable.");
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Class<?> 
    returnedClass()
    {
        return PostalAddress.class;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    equals(Object x,Object y) 
        throws HibernateException
    {
        if ( x == y )
            return true;
        
        if ( x == null || y == null )
            return false;
        
        return x.equals( y );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public int 
    hashCode(Object x) 
        throws HibernateException
    {
        if ( x == null )
            return 0;
        
        return x.hashCode();
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Object 
    nullSafeGet(
        ResultSet          rs,
        String[]           names,
        SessionImplementor session,
        Object             owner) 
            throws 
                HibernateException,
                SQLException
    {
        String address     = null;
        String street      = null;
        String city        = null;
        String state       = null;
        String countryCode = null;
        String postalCode  = null;
        
        address = rs.getString(names[0]);
        
        if ( rs.wasNull() )
            address = null;
        
        street = rs.getString( names[1] );
        
        if ( rs.wasNull() )
            street = null;
        
        city = rs.getString( names[2] );
        
        if ( rs.wasNull() )
            city = null;
        
        state = rs.getString( names[3] );
        
        if ( rs.wasNull() )
            state = null;
        
        countryCode = rs.getString( names[4] );
        
        if ( rs.wasNull() )
            countryCode = null;
        
        postalCode = rs.getString( names[5] );
        
        if ( rs.wasNull() )
            postalCode = null;
        
        if (
            address == null && 
            street == null && 
            city == null && 
            state == null &&
            countryCode == null &&
            postalCode == null )
            return null;
        
        return 
            new PostalAddress(
                address,
                street,
                city,
                state,
                countryCode,
                postalCode);
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    nullSafeSet(
        PreparedStatement  st,
        Object             value,
        int                index,
        SessionImplementor session) 
            throws 
                HibernateException,
                SQLException
    {
        if ( value == null )
        {
            st.setNull( index,new StringType().sqlType() );
            st.setNull( index+1,new StringType().sqlType() );
            st.setNull( index+2,new StringType().sqlType() );
            st.setNull( index+3,new StringType().sqlType() );
            st.setNull( index+4,new StringType().sqlType() );
            st.setNull( index+5,new StringType().sqlType() );
        }
        else
        {
            st.setString(index,((PostalAddress)value).getAddress());
            st.setString(index+1,((PostalAddress)value).getStreet());
            st.setString(index+2,((PostalAddress)value).getCity());
            st.setString(index+3,((PostalAddress)value).getStateProvince());
            st.setString(index+4,((PostalAddress)value).getCountryCode());
            st.setString(index+5,((PostalAddress)value).getPostalCode());
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Object 
    deepCopy(Object value) 
        throws HibernateException
    {
        return (PostalAddress)value;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    isMutable()
    {
        return false;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Serializable 
    disassemble(
        Object             value,
        SessionImplementor session)
            throws HibernateException
    {
        return (PostalAddress)value;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Object 
    assemble(
        Serializable       cached,
        SessionImplementor session,
        Object             owner) 
            throws HibernateException
    {
        return (PostalAddress)cached;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Object 
    replace(
        Object             original,
        Object             target,
        SessionImplementor session,
        Object             owner) 
            throws HibernateException
    {
        return (PostalAddress)original;
    }

}

// ##########################################################################
