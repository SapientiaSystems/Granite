// ##########################################################################
// # File Name:	ServiceInvokerProvider.java
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

package indentitymanager.integration.serviceinvoker;

import strata1.injector.container.IContainer;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class ServiceInvokerProvider 
    implements IServiceInvokerProvider
{
    private final IContainer itsContainer;
    private final String     itsCommandSession;
    private final String     itsRequestChannelId;
    private final String     itsReplyChannelId;
    private final String     itsEventSession;
    private final String     itsEventChannelId;
    
    /************************************************************************
     * Creates a new ServiceInvokerProvider. 
     * @param eventChannelId TODO
     *
     */
    public 
    ServiceInvokerProvider(
        IContainer container,
        String     commandSession,
        String     requestChannelId,
        String     replyChannelId, 
        String     eventSession,
        String     eventChannelId)
    {
        itsContainer        = container;
        itsCommandSession   = commandSession;
        itsRequestChannelId = requestChannelId;
        itsReplyChannelId   = replyChannelId;
        itsEventSession     = eventSession;
        itsEventChannelId   = eventChannelId;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public ServiceInvoker 
    get()
    {
        return 
            new ServiceInvoker(
                itsContainer,
                itsCommandSession,
                itsRequestChannelId,
                itsReplyChannelId,
                itsEventSession,
                itsEventChannelId);
    }
}

// ##########################################################################
