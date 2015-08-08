// ##########################################################################
// # File Name:	UserIdentityEvent.java
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
import strata1.common.datetime.DateTime;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class UserIdentityEvent
    implements IUserIdentityEvent
{
    protected Long                  itsEventId;
    protected Integer               itsVersion;
    protected DateTime              itsTimestamp;
    protected UserIdentityEventKind itsEventKind;
    protected UserIdentityStateKind itsStateKind;
    protected String                itsUserId;
    protected String                itsUserName;
    protected String                itsPassword;
    protected PersonName            itsName;
    protected EmailAddress          itsEmailAddress;
    protected String                itsEmailVerification;
    protected PostalAddress         itsPostalAddress;
    protected PhoneNumber           itsPhoneNumber;
    protected String                itsPhoneVerification;
    protected SecurityQuestion      itsSecurityQuestion;

    /************************************************************************
     * Creates a new UserIdentityEvent. 
     *
     */
    public 
    UserIdentityEvent()
    {
        itsEventId           = 0L;
        itsVersion           = 1;
        itsTimestamp         = new DateTime();
        itsEventKind         = null;
        itsStateKind         = null;
        itsUserId            = null;
        itsUserName          = null;
        itsPassword          = null;
        itsName              = null;
        itsEmailAddress      = null;
        itsEmailVerification = null;
        itsPostalAddress     = null;
        itsPhoneNumber       = null;
        itsPhoneVerification = null;
        itsSecurityQuestion  = null;
    }

    /************************************************************************
     * Creates a new UserIdentityEvent. 
     *
     * @param other
     */
    public 
    UserIdentityEvent(UserIdentityEvent other)
    {
        itsEventId           = other.itsEventId;
        itsVersion           = other.itsVersion;
        itsTimestamp         = other.itsTimestamp;
        itsEventKind         = other.itsEventKind;
        itsStateKind         = other.itsStateKind;
        itsUserId            = other.itsUserId;
        itsUserName          = other.itsUserName;
        itsPassword          = other.itsPassword;
        itsName              = other.itsName;
        itsEmailAddress      = other.itsEmailAddress;
        itsEmailVerification = other.itsEmailVerification;
        itsPostalAddress     = other.itsPostalAddress;
        itsPhoneNumber       = other.itsPhoneNumber;
        itsPhoneVerification = other.itsPhoneVerification;
        itsSecurityQuestion  = other.itsSecurityQuestion;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public UserIdentityEvent 
    copy()
    {
        return new UserIdentityEvent( this );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    setEventId(Long eventId)
    {
        itsEventId = eventId;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    setVersion(Integer version)
    {
        itsVersion = version;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    setTimestamp(DateTime timestamp)
    {
        itsTimestamp = timestamp;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    setEventKind(UserIdentityEventKind eventKind)
    {
        itsEventKind = eventKind;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    setStateKind(UserIdentityStateKind stateKind)
    {
        itsStateKind = stateKind;
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
    setEmailVerification(String code)
    {
        itsEmailVerification = code;
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
    setPhoneVerification(String code)
    {
        itsPhoneVerification = code;
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
    public Long 
    getEventId()
    {
        return itsEventId;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public Integer 
    getVersion()
    {
        return itsVersion;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public DateTime 
    getTimestamp()
    {
        return itsTimestamp;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public UserIdentityEventKind 
    getEventKind()
    {
        return itsEventKind;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public UserIdentityStateKind 
    getStateKind()
    {
        return itsStateKind;
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
    public String 
    getEmailVerification()
    {
        return itsEmailVerification;
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
    public String 
    getPhoneVerification()
    {
        return itsPhoneVerification;
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

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    verifyEmailAddress(String code) 
        throws VerificationException
    {
        if ( getEmailVerification() == null )
            throw 
                new VerificationException( 
                    "No email verification code available" );
        
        if ( !getEmailVerification().equals( code ) )
            throw
                new VerificationException(
                    "Email verification failed:\n" + 
                    "The provided email verification code: " + 
                    code +
                    " does not match the expected verfication code.");
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    verifyPhoneNumber(String code) 
        throws VerificationException
    {
        if ( getPhoneVerification() == null )
            throw 
                new VerificationException( 
                    "No phone verification code available" );
        
        if ( !getPhoneVerification().equals( code ) )
            throw
                new VerificationException(
                    "Phone verification failed:\n" + 
                    "The provided phone verification code: " + 
                    code +
                    " does not match the expected verfication code.");
    }

}

// ##########################################################################
