//  ##########################################################################
//  # File Name: IGroup.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Role;
using Granite.Domain.Shared;
using Granite.Domain.Tenant;
using System;
using System.Collections.Generic;

namespace Granite.Domain.Group
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IGroup:
        IMultiTenantEntity<string>,
        IGroupIdProvider,
        ITenantIdProvider,
        IUserIdSetProvider,
        IEquatable<IGroup>
    {
        ISet<Guid>   MemberUserIds { get; set; }
        ISet<IGroup> IncludedGroups { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IGroup
        MakeMember(IUserIdProvider user);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IGroup
        MakeMembers(IUserIdSetProvider users);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IGroup
        IncludeGroup(IGroup includedGroup);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IGroup
        AttachRole(IRole role);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IGroup
        AttachRoles(ISet<IRole> roles);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ICollection<IRole>
        GetRoles();

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        Initialize(IRoleRepository roles);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        Includes(IGroup group);
    }
}

//  ##########################################################################
