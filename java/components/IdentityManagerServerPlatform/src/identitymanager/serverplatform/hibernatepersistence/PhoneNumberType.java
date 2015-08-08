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

import identitymanager.common.value.PersonName;
import identitymanager.common.value.PhoneNumber;
import strata1.common.money.Money;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.CurrencyType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class PhoneNumberType 
    implements CompositeUserType
{
    /************************************************************************
     * Creates a new {@code DateTimeType}. 
     *
     */
    public
    PhoneNumberType() {}
    
    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String[] 
    getPropertyNames()
    {
        return new String[] {"CountryCode","AreaCode","SubscriberNumber"};
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Type[] 
    getPropertyTypes()
    {
        return 
            new Type[] {new StringType(),new StringType(),new StringType()};
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Object 
    getPropertyValue(Object component,int property)
        throws HibernateException
    {
        PhoneNumber phone = (PhoneNumber)component;
        
        if ( property == 0) 
            return phone.getCountryCode();
        
        if ( property == 1 )
            return phone.getAreaCode();
        
        if ( property == 2 )
            return phone.getSubscriberNumber();
        
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
        throw new HibernateException("PhoneNumber is immutable.");
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Class<?> 
    returnedClass()
    {
        return PersonName.class;
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
        String countryCode      = null;
        String areaCode         = null;
        String subscriberNumber = null;
        
        countryCode = rs.getString(names[0]);
        
        if ( rs.wasNull() )
            countryCode = null;
        
        areaCode = rs.getString( names[1] );
        
        if ( rs.wasNull() )
            areaCode = null;
        
        subscriberNumber = rs.getString( names[2] );
        
        if ( rs.wasNull() )
            subscriberNumber = null;
        
        if ( 
            countryCode == null && 
            areaCode == null && 
            subscriberNumber == null )
            return null;
        
        return new PhoneNumber(countryCode,areaCode,subscriberNumber);
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
        }
        else
        {
            st.setString(index,((PhoneNumber)value).getCountryCode());
            st.setString(index+1,((PhoneNumber)value).getAreaCode());
            st.setString(index+2,((PhoneNumber)value).getSubscriberNumber());
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
        return (PhoneNumber)value;
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
        return (PhoneNumber)value;
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
        return (PhoneNumber)cached;
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
        return (PhoneNumber)original;
    }

}

// ##########################################################################
