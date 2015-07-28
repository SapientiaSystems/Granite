// ##########################################################################
// # File Name:	UserIdentity.java
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

import identitymanager.common.value.EmailAddress;
import identitymanager.common.value.PersonName;
import identitymanager.common.value.PhoneNumber;
import identitymanager.common.value.PostalAddress;
import identitymanager.common.value.SecurityQuestion;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class UserIdentity
    implements IUserIdentity
{
    protected String           itsUserId;
    protected Long             itsVersion;
    protected String           itsUserName;
    protected String           itsPassword;
    protected PersonName       itsName;
    protected EmailAddress     itsEmailAddress;
    protected PostalAddress    itsPostalAddress;
    protected PhoneNumber      itsPhoneNumber;
    protected SecurityQuestion itsSecurityQuestion;
    
    /************************************************************************
     * Creates a new UserIdentity. 
     *
     */
    public 
    UserIdentity()
    {
        itsUserId           = "";
        itsVersion          = 0L;
        itsUserName         = "";
        itsPassword         = "";
        itsName             = null;
        itsEmailAddress     = null;
        itsPostalAddress    = null;
        itsPhoneNumber      = null;
        itsSecurityQuestion = null;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    setUserId(String userId)
    {
        itsUserId = userId;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    setVersion(Long version)
    {
        itsVersion = version;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    setUserName(String userName)
    {
        itsUserName = userName;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    setPassword(String password)
    {
        itsPassword = password;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    setName(PersonName name)
    {
        itsName = name;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    setEmailAddress(EmailAddress email)
    {
        itsEmailAddress = email;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    setPostalAddress(PostalAddress post)
    {
        itsPostalAddress = post;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    setPhoneNumber(PhoneNumber phone)
    {
        itsPhoneNumber = phone;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    setSecurityQuestion(SecurityQuestion securityQuestion)
    {
        itsSecurityQuestion = securityQuestion;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String 
    getUserId()
    {
        return itsUserId;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Long 
    getVersion()
    {
        return itsVersion;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String 
    getUserName()
    {
        return itsUserName;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String 
    getPassword()
    {
        return itsPassword;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public PersonName 
    getName()
    {
        return itsName;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public EmailAddress 
    getEmailAddress()
    {
        return itsEmailAddress;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public PostalAddress 
    getPostalAddress()
    {
        return itsPostalAddress;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public PhoneNumber 
    getPhoneNumber()
    {
        return itsPhoneNumber;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public SecurityQuestion 
    getSecurityQuestion()
    {
        return itsSecurityQuestion;
    }

}

// ##########################################################################
