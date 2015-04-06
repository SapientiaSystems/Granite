// ##########################################################################
// # File Name:	SubjectFactory.java
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

package identitymanager.service.authentication;

import identitymanager.service.subject.IPermission;
import identitymanager.service.subject.IRole;
import identitymanager.service.subject.ResourceActionPermission;
import identitymanager.service.subject.Role;
import identitymanager.service.user.IUser;
import identitymanager.service.user.User;
import identitymanager.service.user.UserName;

/****************************************************************************
 * 
 * @author 		
 *     Sapientia Systems
 * @conventions	
 *     <a href="{@docRoot}/NamingConventions.html">Naming Conventions</a>
 */
public 
class SubjectFactory
    implements ISubjectFactory
{

    /************************************************************************
     * Creates a new SubjectFactory. 
     *
     */
    public 
    SubjectFactory() {}

    /************************************************************************
     * {@inheritDoc} 
     */
    @Override
    public IUser 
    createUser(UserDescriptor descriptor)
    {
        User user = new User( new UserName(descriptor.getUserName()));
        
        for (RoleDescriptor roleDescriptor:descriptor.getRoles())
            user.assignRole( createRole(roleDescriptor) );
        
        return user;
    }

    /************************************************************************
     *  
     *
     * @param descriptor
     * @return
     */
    protected IRole
    createRole(RoleDescriptor descriptor)
    {
        Role role = new Role( descriptor.getRoleName() );
        
        for (PermissionDescriptor permDescriptor:descriptor.getPermissions())
            role.grantPermission(createPermission(permDescriptor));
        
        return role;
    }
    
    /************************************************************************
     *  
     *
     * @param descriptor
     * @return
     */
    protected IPermission
    createPermission(PermissionDescriptor descriptor)
    {
        ResourceActionPermission permission = 
            new ResourceActionPermission( descriptor.getPermissionName() );
                
        return permission;
    }
}

// ##########################################################################
