// ##########################################################################
// # File Name:	FakeUserIdentityReplyReceiver.java
// #
// # Copyright:	2015, Sapientia Systems, LLC. All Rights Reserved.
// #
// # License:	This file is part of the IdentityManagerServiceTest Framework.
// #
// #   			The IdentityManagerServiceTest Framework is free software: you 
// #			can redistribute it and/or modify it under the terms of 
// #			the GNU Lesser General Public License as published by
// #    		the Free Software Foundation, either version 3 of the 
// #			License, or (at your option) any later version.
// #
// #    		The IdentityManagerServiceTest Framework is distributed in the 
// #			hope that it will be useful, but WITHOUT ANY WARRANTY; 
// #			without even the implied warranty of MERCHANTABILITY or 
// #			FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// #			General Public License for more details.
// #
// #    		You should have received a copy of the GNU Lesser 
// #			General Public License along with the IdentityManagerServiceTest
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
public 
class FakeUserIdentityReplyReceiver
    extends AbstractUserIdentityReplyReceiver
{
    private UserIdentityReply itsReply;
    
    /************************************************************************
     * Creates a new FakeUserIdentityReplyReceiver. 
     *
     */
    public 
    FakeUserIdentityReplyReceiver()
    {
        itsReply = null;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onStartCreatingUserIdentity(StartCreatingUserIdentityReply reply)
    {
        itsReply = reply;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onStartListening(StartListeningReply reply)
    {
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public void 
    onStopListening(StopListeningReply reply)
    {
    }

    public void
    validateReply(UserIdentityReply expected)
    {
        if ( itsReply == null )
            throw new AssertionError("reply is null");
        
        if ( !itsReply.getClass().equals( expected.getClass() ) )
            throw new AssertionError( "reply class does not match expected" );
    }
}

// ##########################################################################
