// ##########################################################################
// # File Name:	DefaultTimestampGenerator.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerCommon Framework.
// #
// #   			The IdentityManagerCommon Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerCommon Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerCommon
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.common.utility;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class DefaultTimestampGenerator
    implements ITimestampGenerator
{

    /************************************************************************
     * Creates a new DefaultTimestampGenerator. 
     *
     */
    public 
    DefaultTimestampGenerator() {}

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public XMLGregorianCalendar 
    getCurrent()
    {
        try
        {
            GregorianCalendar current = new GregorianCalendar();
            DatatypeFactory   factory = DatatypeFactory.newInstance();
            
            return factory.newXMLGregorianCalendar(current);
        }
        catch(DatatypeConfigurationException e)
        {
            return null;
        }
    }

}

// ##########################################################################
