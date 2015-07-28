// ##########################################################################
// # File Name:	JaxbTransferObjectMapper.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerIntegration Framework.
// #
// #   			The IdentityManagerIntegration Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerIntegration Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerIntegration
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.integration.mapper;

import identitymanager.service.shared.IdentityManagerReply;
import identitymanager.service.useridentity.FinishCreatingUserIdentityReply;
import identitymanager.service.useridentity.FinishUpdatingUserIdentityReply;
import identitymanager.service.useridentity.StartCreatingUserIdentityReply;
import identitymanager.service.useridentity.StartListeningReply;
import identitymanager.service.useridentity.StartUpdatingUserIdentityReply;
import identitymanager.service.useridentity.StopListeningReply;
import identitymanager.service.useridentity.UpdateUserIdentityReply;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class JaxbReplyMapper
    implements IReplyMapper
{
    private static final QName REPLY_QNAME = 
        new QName(
            "http://IdentityManager.Service.Shared", 
            "Reply");
    
    private JAXBContext   itsContext;
    private Marshaller    itsMarshaller;
    private Unmarshaller  itsUnmarshaller;

    /************************************************************************
     * Creates a new JaxbTransferObjectMapper. 
     * @throws JAXBException 
     *
     */
    public 
    JaxbReplyMapper() 
        throws MappingException
    {
        try
        {
            itsContext = JAXBContext.newInstance(
                             StartCreatingUserIdentityReply.class,
                             FinishCreatingUserIdentityReply.class,
                             StartUpdatingUserIdentityReply.class,
                             FinishUpdatingUserIdentityReply.class,
                             UpdateUserIdentityReply.class,
                             StartListeningReply.class,
                             StopListeningReply.class);
            
            itsMarshaller   = itsContext.createMarshaller();
            itsUnmarshaller = itsContext.createUnmarshaller();
        }
        catch (Exception e)
        {
            throw new MappingException( e );
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public <T extends IdentityManagerReply> String 
    toPayload(T transferObject)
        throws MappingException
    {
        JAXBElement<IdentityManagerReply> element = null;   
        StringWriter                       writer  = null;
        
        try
        {
            element = 
                new JAXBElement<IdentityManagerReply>(
                    REPLY_QNAME,
                    IdentityManagerReply.class, 
                    null, 
                    transferObject);
            writer  = new StringWriter();
            
            itsMarshaller.marshal( element,writer );       
            return writer.toString();
        }
        catch (Exception e)
        {
            throw new MappingException( e );
        }
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IdentityManagerReply> T 
    toReply(Class<T> replyType,String payload)
        throws MappingException
    {
        StringReader                      reader  = null;
        JAXBElement<IdentityManagerReply> element = null;

        try
        {
            reader  = new StringReader(payload);
            element = 
                (JAXBElement<IdentityManagerReply>)
                    itsUnmarshaller.unmarshal(reader);
            
            return replyType.cast( element.getValue() );

        }
        catch (Exception e)
        {
            throw new MappingException( e );
        }
    }

}

// ##########################################################################
