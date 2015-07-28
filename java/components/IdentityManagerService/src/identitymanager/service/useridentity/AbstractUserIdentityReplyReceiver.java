// ##########################################################################
// # File Name:	AbstractUserIdentityReplyReceiver.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerService Framework.
// #
// #   			The IdentityManagerService Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerService Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerService
// #			Framework. If not, see http://www.gnu.org/licenses/.
// ##########################################################################

package identitymanager.service.useridentity;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public abstract 
class AbstractUserIdentityReplyReceiver
    implements IUserIdentityReplyReceiver
{

    /************************************************************************
     * Creates a new AbstractUserIdentityReplyReceiver. 
     *
     */
    protected
    AbstractUserIdentityReplyReceiver()
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onStartCreatingUserIdentity(StartCreatingUserIdentityReply reply)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onFinishCreatingUserIdentity(FinishCreatingUserIdentityReply reply)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onStartUpdatingUserIdentity(StartUpdatingUserIdentityReply reply)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onFinishUpdatingUserIdentity(FinishUpdatingUserIdentityReply reply)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onUpdateUserIdentity(UpdateUserIdentityReply reply)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onUserIdentityException(UserIdentityException exception)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onException(Exception exception)
    {
    }

}

// ##########################################################################
