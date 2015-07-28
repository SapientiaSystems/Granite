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

import identitymanager.service.shared.IdentityManagerEvent;
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
class JaxbEventMapper
    implements IEventMapper
{
    private static final QName EVENT_QNAME = 
        new QName(
            "http://IdentityManager.Service.Shared", 
            "Event");
    
    private JAXBContext   itsContext;
    private Marshaller    itsMarshaller;
    private Unmarshaller  itsUnmarshaller;

    /************************************************************************
     * Creates a new JaxbTransferObjectMapper. 
     * @throws JAXBException 
     *
     */
    public 
    JaxbEventMapper() 
        throws MappingException
    {
        try
        {
            itsContext = JAXBContext.newInstance(
                             IdentityManagerEvent.class);
            
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
    public <T extends IdentityManagerEvent> String 
    toPayload(T transferObject)
        throws MappingException
    {
        JAXBElement<IdentityManagerEvent> element = null;   
        StringWriter                       writer  = null;
        
        try
        {
            element = 
                new JAXBElement<IdentityManagerEvent>(
                    EVENT_QNAME,
                    IdentityManagerEvent.class, 
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
    public <T extends IdentityManagerEvent> T 
    toEvent(Class<T> eventType,String payload)
        throws MappingException
    {
        StringReader                      reader  = null;
        JAXBElement<IdentityManagerEvent> element = null;

        try
        {
            reader  = new StringReader(payload);
            element = 
                (JAXBElement<IdentityManagerEvent>)
                    itsUnmarshaller.unmarshal(reader);
            
            return eventType.cast( element.getValue() );

        }
        catch (Exception e)
        {
            throw new MappingException( e );
        }
    }

}

// ##########################################################################
