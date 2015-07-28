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

import strata1.common.datetime.DateTime;
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
interface IUserIdentityEvent
{
    public void
    setEventId(Long eventId);
    
    public void
    setVersion(Long version);
    
    public void
    setTimeStamp(DateTime timestamp);
    
    public void
    setEventKind(UserIdentityEventKind eventKind);
    
    public void
    setStateKind(UserIdentityStateKind stateKind);
    
    public void
    setUserId(String userId);
    
    public void
    setUserName(String userName);
    
    public void
    setPassword(String password);
    
    public void
    setName(PersonName name);
    
    public void
    setEmailAddress(EmailAddress email);
    
    public void
    setEmailVerification(String code);
    
    public void
    setPostalAddress(PostalAddress post);
    
    public void
    setPhoneNumber(PhoneNumber phone);
    
    public void
    setPhoneVerification(String code);
    
    public void
    setSecurityQuestion(SecurityQuestion securityQuestion);
    
    public Long
    getEventId();
    
    public Long
    getVersion();
    
    public DateTime
    getTimestamp();
    
    public UserIdentityEventKind
    getEventKind();
    
    public UserIdentityStateKind
    getStateKind();
    
    public String
    getUserId();
    
    public String
    getUserName();
    
    public String
    getPassword();
    
    public PersonName
    getName();
    
    public EmailAddress
    getEmailAddress();
    
    public String
    getEmailVerification();
    
    public PostalAddress
    getPostalAddress();
    
    public PhoneNumber
    getPhoneNumber();
    
    public String
    getPhoneVerification();
    
    public SecurityQuestion
    getSecurityQuestion();
    
    public void
    verifyEmailAddress(String code)
        throws VerificationException;
    
    public void
    verifyPhoneNumber(String code)
        throws VerificationException;
}

// ##########################################################################
