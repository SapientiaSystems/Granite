//  ##########################################################################
//  # File Name: UserMapper.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Group;
using Granite.Domain.Role;
using Granite.Domain.User;
using Granite.ProvidedService.User;
using System;
using System.Collections.Generic;
using System.Linq;

namespace Granite.Application.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class UserMapper:
        IUserMapper
    {
        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///
        public UserData 
        Map(IUser user)
        {
            if (user == null)
                throw
                    new NullReferenceException("Cannot map a null user.");

            return
                new UserData()
                {
                    TenantId = user.TenantId,
                    UserId = user.UserId,
                    UserName = user.UserName,
                    Email = 
                        user.Email != null 
                            ? user.Email.ToString()
                            : null,
                    Roles = GetRoles(user),
                    Groups = GetGroups(user)
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        protected UserRoleData[]
        GetRoles(IUser user)
        {
            IList<UserRoleData> roles = new List<UserRoleData>();

            foreach (IRole role in user.GetRoles())
                roles.Add(
                    new UserRoleData()
                    {
                        RoleName = role.RoleName,
                        ConstraintId =
                            user
                                .GetConstraint(role)
                                .ConstraintId
                    });

            return roles.ToArray();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        protected UserRoleData[]
        GetRoles(IUser user,IGroup group)
        {
            IList<UserRoleData> roles = new List<UserRoleData>();

            foreach (IRole role in group.GetRoles())
                roles.Add(
                    new UserRoleData()
                    {
                        RoleName = role.RoleName,
                        ConstraintId =
                            user
                                .GetConstraint(role)
                                .ConstraintId
                    });

            return roles.ToArray();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        protected UserGroupData[]
        GetGroups(IUser user)
        {
            IList<UserGroupData> groups = new List<UserGroupData>();

            foreach (IGroup group in user.GetGroups())
            {
                groups.Add(
                    new UserGroupData()
                    {
                        TenantId = group.TenantId,
                        GroupName = group.GroupId.EntityId,
                        Roles = GetRoles(user,group)
                    });
            }

            return groups.ToArray();

        }
    }
}

//  ##########################################################################
