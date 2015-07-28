// ##########################################################################
// # File Name:	EmailMessage.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerServerPlatform Framework.
// #
// #   			The IdentityManagerServerPlatform Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerServerPlatform Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerServerPlatform
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.serverplatform.javamail;

import identitymanager.application.email.HtmlEmailContent;
import identitymanager.application.email.IEmailContent;
import identitymanager.application.email.IEmailMessage;
import identitymanager.application.email.IEmailSession;
import identitymanager.application.email.RecipientKind;
import identitymanager.application.email.TextEmailContent;
import identitymanager.common.value.EmailAddress;
import strata1.common.utility.IMultiMap;
import strata1.common.utility.MultiMap;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class JavaMailMessage
    implements IEmailMessage
{    
    private final IEmailSession itsSession;
    private final MimeMessage   itsImp;
    private final Multipart     itsMultipart;
    private IEmailContent       itsContent;
    
    /************************************************************************
     * Creates a new MultiPartMimeEmailMessage. 
     *
     */
    public 
    JavaMailMessage(IEmailSession session)
    {
        itsSession   = session;
        itsImp       = new MimeMessage(((JavaMailSession)itsSession).getImp());
        itsMultipart = new MimeMultipart("alternative");
        itsContent   = null;
        
        try
        {
            itsImp.setContent(itsMultipart);
        }
        catch(MessagingException e)
        {
            throw new IllegalStateException(e);
        }   
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IEmailMessage 
    insertRecipient(RecipientKind kind, EmailAddress recipient)
    {
        try
        {
            itsImp.addRecipient( 
                toRecipientType( kind ),
                new InternetAddress(recipient.toString()) );
            
            return this;
        }
        catch(MessagingException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IEmailMessage 
    setFrom(EmailAddress from)
    {
        try
        {
            itsImp.setFrom( new InternetAddress(from.toString() ) );
            return this;
        }
        catch(MessagingException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IEmailMessage 
    setSubject(String subject)
    {
        try
        {
            itsImp.setSubject( subject );
            return this;
        }
        catch(MessagingException e)
        {
            throw new IllegalStateException(e);
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IEmailMessage 
    setContent(IEmailContent content)
    {
        if ( content.isText() )
            addTextPart( (TextEmailContent)content );
        else if ( content.isHtml() )
            addHtmlPart( (HtmlEmailContent)content );
        
        itsContent = content;
        return this;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IEmailMessage 
    attachFile(File file)
    {
        try
        {
            MimeBodyPart part = new MimeBodyPart();
            
            part.attachFile( file );
            return this;
        }
        catch(IOException e)
        {
            throw new IllegalStateException( e );
        }
        catch(MessagingException e)
        {
            throw new IllegalStateException( e );
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IEmailSession 
    getSession()
    {
        return itsSession;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IMultiMap<RecipientKind,EmailAddress> 
    getRecipients()
    {
        try
        {
            Address[]                             to     = null;
            Address[]                             cc     = null;
            Address[]                             bcc    = null;
            IMultiMap<RecipientKind,EmailAddress> output = null;
            
            to     = itsImp.getRecipients( RecipientType.TO );
            cc     = itsImp.getRecipients( RecipientType.CC );
            bcc    = itsImp.getRecipients( RecipientType.BCC );           
            output = new MultiMap<RecipientKind,EmailAddress>();

            for (Address recipient:to)
                output.put( 
                    RecipientKind.TO,
                    new EmailAddress( recipient.toString() ) );

            for (Address recipient:cc)
                output.put( 
                    RecipientKind.CC,
                    new EmailAddress( recipient.toString() ) );

            for (Address recipient:bcc)
                output.put( 
                    RecipientKind.BCC,
                    new EmailAddress( recipient.toString() ) );
             
            return output;
        }
        catch(MessagingException e)
        {
            throw new IllegalStateException( e );
        }       
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public List<EmailAddress> 
    getRecipients(RecipientKind kind)
    {
        try
        {
            Address[]          recipients = null;
            List<EmailAddress> output     = null;

            recipients = itsImp.getRecipients( toRecipientType( kind ) );
            output     = new ArrayList<EmailAddress>();

            for (Address recipient : recipients)
                output.add( new EmailAddress( recipient.toString() ) );

            return output;
        }
        catch(MessagingException e)
        {
            throw new IllegalStateException( e );
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public EmailAddress 
    getFrom()
    {
        try
        {
            Address[] from = itsImp.getFrom();
            
            return new EmailAddress( from[0].toString() );
        }
        catch(MessagingException e)
        {
            throw new IllegalStateException( e );
        }       
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public String 
    getSubject()
    {
        try
        {
            return itsImp.getSubject();
        }
        catch(MessagingException e)
        {
            throw new IllegalStateException( e );
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IEmailContent 
    getContent()
    {
        return itsContent;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public List<File> 
    getAttachedFiles()
    {
        try
        {
            List<File> files = new ArrayList<File>();
            
            for (int i=0;i<itsMultipart.getCount();i++)
            {
                BodyPart part = itsMultipart.getBodyPart( i );
                
                if ( part.getFileName() != null )
                    files.add( new File(part.getFileName()) );
            }
            
            return files;
        }
        catch(MessagingException e)
        {
            throw new IllegalStateException( e );
        }
    }

    /************************************************************************
     *  
     *
     * @return
     */
    public MimeMessage
    getImp()
    {
        return itsImp;
    }
    
    /************************************************************************
     *  
     *
     * @param kind
     * @return
     */
    private RecipientType
    toRecipientType(RecipientKind kind)
    {
        switch (kind)
        {
        case TO:
            return RecipientType.TO;
        case CC:
            return RecipientType.CC;
        default:
            return RecipientType.BCC;
        }
    }

    /************************************************************************
     *  
     *
     * @param content
     */
    private void 
    addTextPart(TextEmailContent content)
    {
        MimeBodyPart part = new MimeBodyPart();
        
        try
        {
            part.setText( content.getText() );
            itsMultipart.addBodyPart( part );
        }
        catch(MessagingException e)
        {
            throw new IllegalStateException( e );
        }
    }

    /************************************************************************
     *  
     *
     * @param content
     */
    private void 
    addHtmlPart(HtmlEmailContent content)
    {
        MimeBodyPart part = new MimeBodyPart();
        
        try
        {
            part.setContent( content.getHtml(),"text/html" );
            itsMultipart.addBodyPart( part );
        }
        catch(MessagingException e)
        {
            throw new IllegalStateException( e );
        }
    }
}

// ##########################################################################
