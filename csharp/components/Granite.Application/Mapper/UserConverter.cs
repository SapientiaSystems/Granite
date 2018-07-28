//  ##########################################################################
//  # File Name: UserConverter.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using AutoMapper;
using Granite.Domain.Group;
using Granite.Domain.Role;
using Granite.Domain.User;
using Granite.ProvidedService.User;
using System;
using System.Collections.Generic;
using System.Linq;

namespace Granite.Application.Mapper
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class UserConverter:
        ITypeConverter<IUser,UserData>
    {

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///
        public UserData 
        Convert(IUser source,UserData destination,ResolutionContext context)
        {
            if (source == null)
                return null;

            destination.TenantId = source.TenantId;
            destination.UserId = source.UserId;
            destination.UserName = source.UserName;
            destination.Email =
                source.Email != null
                    ? source.Email.ToString()
                    : null;
            destination.Roles = GetRoles(source);
            destination.Groups = GetGroups(source);

            return destination;
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
