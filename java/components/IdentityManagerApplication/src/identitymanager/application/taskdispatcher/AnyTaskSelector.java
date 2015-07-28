// ##########################################################################
// # File Name:	AnyTaskSelector.java
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

package identitymanager.application.taskdispatcher;

import strata1.common.task.ITask;
import strata1.common.task.ITaskSelector;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class AnyTaskSelector 
    implements ITaskSelector
{

    /************************************************************************
     * Creates a new AnyTaskSelector. 
     *
     */
    public 
    AnyTaskSelector() {}

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    match(ITask task)
    {
        return true;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public boolean 
    equals(Object other)
    {
        return other instanceof AnyTaskSelector;
    }

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public int 
    hashCode()
    {
        return 1234567;
    }

}

// ##########################################################################
