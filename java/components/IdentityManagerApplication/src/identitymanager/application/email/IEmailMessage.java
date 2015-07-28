// ##########################################################################
// # File Name:	IEmailMessage.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerApplication Framework.
// #
// #   			The IdentityManagerApplication Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerApplication Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerApplication
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.application.email;

import identitymanager.common.value.EmailAddress;
import strata1.common.utility.IMultiMap;
import java.io.File;
import java.util.List;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
interface IEmailMessage
{
    public IEmailMessage
    insertRecipient(RecipientKind kind,EmailAddress recipient);

    public IEmailMessage
    setFrom(EmailAddress from);
    
    public IEmailMessage
    setSubject(String subject);
    
    public IEmailMessage
    setContent(IEmailContent content);
        
    public IEmailMessage
    attachFile(File file);
    
    public IEmailSession
    getSession();
    
    public IMultiMap<RecipientKind,EmailAddress>
    getRecipients();
    
    public List<EmailAddress>
    getRecipients(RecipientKind kind);
    
    public EmailAddress
    getFrom();
    
    public String
    getSubject();
    
    public IEmailContent
    getContent();
    
    public List<File>
    getAttachedFiles();
    
}

// ##########################################################################
