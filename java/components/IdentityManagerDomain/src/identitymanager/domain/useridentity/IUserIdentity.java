// ##########################################################################
// # File Name:	IUserIdentity.java
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
interface IUserIdentity
{
    public void
    setUserId(String userId);
    
    public void
    setVersion(Long version);
    
    public void
    setUserName(String userName);
    
    public void
    setPassword(String password);
    
    public void
    setName(PersonName name);
    
    public void
    setEmailAddress(EmailAddress email);
    
    public void
    setPostalAddress(PostalAddress post);
    
    public void
    setPhoneNumber(PhoneNumber phone);
    
    public void
    setSecurityQuestion(SecurityQuestion securityQuestion);
    
    public String
    getUserId();
    
    public Long
    getVersion();
    
    public String
    getUserName();
    
    public String
    getPassword();
    
    public PersonName
    getName();
    
    public EmailAddress
    getEmailAddress();
    
    public PostalAddress
    getPostalAddress();
    
    public PhoneNumber
    getPhoneNumber();
    
    public SecurityQuestion
    getSecurityQuestion();
}

// ##########################################################################
