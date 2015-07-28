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

import identitymanager.service.shared.IdentityManagerRequest;
import identitymanager.service.useridentity.FinishCreatingUserIdentityRequest;
import identitymanager.service.useridentity.FinishUpdatingUserIdentityRequest;
import identitymanager.service.useridentity.StartCreatingUserIdentityRequest;
import identitymanager.service.useridentity.StartListeningRequest;
import identitymanager.service.useridentity.StartUpdatingUserIdentityRequest;
import identitymanager.service.useridentity.StopListeningRequest;
import identitymanager.service.useridentity.UpdateUserIdentityRequest;
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
class JaxbRequestMapper
    implements IRequestMapper
{
    private static final QName REQUEST_QNAME = 
        new QName(
            "http://IdentityManager.Service.Shared", 
            "Request");
    
    private JAXBContext   itsContext;
    private Marshaller    itsMarshaller;
    private Unmarshaller  itsUnmarshaller;

    /************************************************************************
     * Creates a new JaxbTransferObjectMapper. 
     * @throws JAXBException 
     *
     */
    public 
    JaxbRequestMapper() 
        throws MappingException
    {
        try
        {
            itsContext = JAXBContext.newInstance(
                             StartCreatingUserIdentityRequest.class,
                             FinishCreatingUserIdentityRequest.class,
                             StartUpdatingUserIdentityRequest.class,
                             FinishUpdatingUserIdentityRequest.class,
                             UpdateUserIdentityRequest.class,
                             StartListeningRequest.class,
                             StopListeningRequest.class);
            
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
    public <T extends IdentityManagerRequest> String 
    toPayload(T transferObject)
        throws MappingException
    {
        JAXBElement<IdentityManagerRequest> element = null;   
        StringWriter                        writer  = null;
        
        try
        {
            element = 
                new JAXBElement<IdentityManagerRequest>(
                    REQUEST_QNAME,
                    IdentityManagerRequest.class, 
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
    public <T extends IdentityManagerRequest> T 
    toRequest(Class<T> requestType,String payload)
        throws MappingException
    {
        StringReader                        reader  = null;
        JAXBElement<IdentityManagerRequest> element = null;

        try
        {
            reader  = new StringReader(payload);
            element = 
                (JAXBElement<IdentityManagerRequest>)
                    itsUnmarshaller.unmarshal(reader);
            
            return requestType.cast( element.getValue() );

        }
        catch (Exception e)
        {
            throw new MappingException( e );
        }
    }

}

// ##########################################################################
