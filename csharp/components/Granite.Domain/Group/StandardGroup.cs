//  ##########################################################################
//  # File Name: StandardGroup.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Role;
using Granite.Domain.Shared;
using Granite.Domain.Tenant;
using Strata.Foundation.Utility;
using System;
using System.Collections.Generic;

namespace Granite.Domain.Group
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class StandardGroup:
        AbstractMultiTenantEntity<string>,
        IGroup
    {
        public virtual MultiTenantId<string> 
                                    GroupId
                                        {get {return MultiTenantPrimaryId;}}
        public virtual Guid         TenantId {get {return GetTenantId();}}

        public virtual ISet<Guid>   MemberUserIds { get; set; }
        public virtual ISet<IGroup> IncludedGroups { get; set; }

        public virtual StringSet    RoleIds { get; set; }

        private IRoleRepository     itsRoleRepository;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        StandardGroup()
        {
            MemberUserIds = new GuidSet();
            IncludedGroups = new HashSet<IGroup>();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        StandardGroup(string groupName)
        {
            PrimaryId = groupName;
            MemberUserIds = new GuidSet();
            IncludedGroups = new HashSet<IGroup>();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        StandardGroup(string groupName,params IGroup[] includedGroups)
        {
            PrimaryId = groupName;
            MemberUserIds = new HashSet<Guid>();
            IncludedGroups = new HashSet<IGroup>(includedGroups);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual ISet<Guid>
        GetUserIds()
        {
            ISet<Guid> output = new HashSet<Guid>(MemberUserIds);

            IncludedGroups.ForEach(
                g => { output.UnionWith(g.GetUserIds()); });

            return output;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual IGroup
        MakeMember(IUserIdProvider user)
        {
            if (MemberUserIds == null)
                MemberUserIds = new GuidSet();

            MemberUserIds.Add(user.UserId);
            return this;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual IGroup
        MakeMembers(IUserIdSetProvider users)
        {
            if (MemberUserIds == null)
                MemberUserIds = new GuidSet();

            users
                .GetUserIds()
                .ForEach(userId => { MemberUserIds.Add(userId); });

            return this;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual IGroup
        IncludeGroup(IGroup includedGroup)
        {
            IncludedGroups.Add(includedGroup);
            return this;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual IGroup
        AttachRole(IRole role)
        {
            CheckRole(role);

            if (RoleIds == null)
                RoleIds = new StringSet();

            RoleIds.Add(role.RoleName);
            return this;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual IGroup
        AttachRoles(ISet<IRole> roles)
        {
            roles.ForEach(r => CheckRole(r));

            if (RoleIds == null)
                RoleIds = new StringSet();

            roles.ForEach(r => RoleIds.Add(r.RoleName));
            return this;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual ICollection<IRole>
        GetRoles()
        {
            return itsRoleRepository.GetRoles(RoleIds);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void
        Initialize(IRoleRepository roles)
        {
            itsRoleRepository = roles;

            if (string.IsNullOrEmpty(PrimaryId))
                PrimaryId = MultiTenantPrimaryId.EntityId;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual bool
        Includes(IGroup group)
        {
            if (group == null)
                return false;

            if (this.Equals(group))
                return true;

            foreach (IGroup included in IncludedGroups)
                if (included != null && included.Includes(group))
                    return true;

            return false;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual bool
        Equals(IGroup other)
        {
            return 
                other != null &&
                MultiTenantPrimaryId.Equals(other.MultiTenantPrimaryId);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override bool
        Equals(object other)
        {
            return other is IGroup && Equals((IGroup)other);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override int
        GetHashCode()
        {
            return 
                new HashCodeBuilder()
                    .Append(MultiTenantPrimaryId.GetHashCode())
                    .HashCode;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual Guid
        GetTenantId()
        {
            return MultiTenantPrimaryId.TenantId;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual void
        Validate(IGroup target,ISet<string> visited)
        {
            if (visited.Contains(target.PrimaryId))
                throw
                    new Exception("Cirular reference not allowed.");
            else
            {
                visited.Add(target.PrimaryId);

                foreach (IGroup included in target.IncludedGroups)
                    Validate(included,visited);
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private void
        CheckRole(IRole role)
        {
            if (!itsRoleRepository.HasRole(role.RoleName))
                throw
                    new ArgumentOutOfRangeException(
                        string.Format(
                            "Role {0} has not been registered.",
                            role.RoleName));
        }

    }
}

//  ##########################################################################
