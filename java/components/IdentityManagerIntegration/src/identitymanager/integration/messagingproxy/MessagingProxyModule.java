// ##########################################################################
// # File Name:	ProxyModule.java
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

package identitymanager.integration.messagingproxy;

import identitymanager.integration.mapper.IEventMapper;
import identitymanager.integration.mapper.IReplyMapper;
import identitymanager.integration.mapper.IRequestMapper;
import identitymanager.common.utility.IIdentifierGenerator;
import identitymanager.common.utility.ITimestampGenerator;
import strata1.injector.container.AbstractModule;
import strata1.injector.container.IContainer;
import strata1.integrator.messaging.IMessagingSession;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public abstract 
class MessagingProxyModule
    extends AbstractModule
{

    /************************************************************************
     * Creates a new ProxyModule. 
     *
     * @param name
     */
    public 
    MessagingProxyModule(String name)
    {
        super( name );
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    initialize(IContainer container)
    {
        checkDependencies( container );
        createProxyBinding( container );
    }

    /************************************************************************
     *  
     *
     * @param container
     */
    protected void 
    checkDependencies(IContainer container)
    {
        if (!container.hasBinding(IMessagingSession.class,"CommandSession"))
            throw new IllegalStateException("CommandSession is null");
        
        if (!container.hasBinding(IMessagingSession.class,"EventSession"))
            throw new IllegalStateException("EventSession is null");
        
        if (!container.hasBinding(String.class,"RequestChannelId"))
            throw new IllegalStateException("RequestChannelId is null");
        
        if (!container.hasBinding(String.class,"ReplyChannelId"))
            throw new IllegalStateException("ReplyChannelId is null");
        
        if (!container.hasBinding(String.class,"EventChannelId"))
            throw new IllegalStateException("EventChannelId is null");
        
        if (!container.hasBinding(IRequestMapper.class))
            throw new IllegalStateException("RequestMapper is null");
        
        if (!container.hasBinding(IReplyMapper.class))
            throw new IllegalStateException("ReplyMapper is null");
        
        if (!container.hasBinding(IEventMapper.class))
            throw new IllegalStateException("EventMapper is null");
        
        if (!container.hasBinding(IIdentifierGenerator.class))
            throw new IllegalStateException("IdentifierGenerator is null");
        
        if (!container.hasBinding(ITimestampGenerator.class))
            throw new IllegalStateException("TimestampGenerator is null");
    }

    /************************************************************************
     *  
     *
     * @param container
     */
    protected abstract void 
    createProxyBinding(IContainer container);

}

// ##########################################################################
