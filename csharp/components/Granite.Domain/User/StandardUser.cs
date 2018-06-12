//  ##########################################################################
//  # File Name: StandardUser.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Constraint;
using Granite.Domain.Group;
using Granite.Domain.Role;
using Granite.Domain.Shared;
using Granite.Domain.Tenant;
using Strata.Domain.Shared;
using Strata.Foundation.Utility;
using Strata.Foundation.Value;
using System;
using System.Collections.Generic;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class StandardUser:
        AbstractEntity<
            Guid,
            IUser,
            IUserDomainEvent,
            IUserDomainEventObserver>,
        IUser
    {
        public virtual Guid           
        UserId
        {
            get { return PrimaryId; }
            set { PrimaryId = value; }
        }

        public virtual Guid           TenantId { get; set; }
        public virtual ITenant        Tenant { get; protected set; }
        public virtual bool           IsTenantValid
                                          { get { return Tenant != null; } }

        public virtual string         UserName { get; set; }
        public virtual HashedString   Password { get; set; }

        public virtual EmailAddress   Email { get; set; }
        public virtual string         ExternalUserId { get; set; }

        public virtual UserStatus     Status { get; set; }
        public virtual string         VerificationCode { get; set; }

        public virtual StringSet      RoleIds { get; set; }
        public virtual ConstraintIdDictionary
                                      ConstraintIds { get; set; }

        public virtual string         PlainTextPassword { get; set; }

        private ITenantRepository     itsTenantRepository;
        private IGroupRepository      itsGroupRepository;
        private IRoleRepository       itsRoleRepository;
        private IConstraintRepository itsConstraintRepository;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        StandardUser() :
            this(null,null,null,null,null)
        { }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        StandardUser(ITenant tenant):
            this(tenant,null,null,null,null) {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        StandardUser(
            ITenant               tenant,
            ITenantRepository     tenants,
            IGroupRepository      groups,
            IRoleRepository       roles,
            IConstraintRepository constraints)
        {
            itsTenantRepository = tenants;
            itsGroupRepository = groups;
            itsRoleRepository = roles;
            itsConstraintRepository = constraints;

            Tenant = tenant;

            if (Tenant != null)
                TenantId = Tenant.TenantId;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual IUser
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
        public virtual IUser
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
        public virtual IUser
        SetConstraint(IRole role,IConstraint constraint)
        {
            if (ConstraintIds == null)
                ConstraintIds = new ConstraintIdDictionary();

            if (IsInRole(role))
            {
                ConstraintIds.Add(role.RoleName,constraint.ConstraintId);
                return this;
            }
            else
            {
                foreach (
                    IGroup group in itsGroupRepository.GetGroupsFor(this))
                    if (IsInRole(group,role))
                    {
                        ConstraintIds.Add(role.RoleName,constraint.ConstraintId);
                        return this;
                    }
            }

            throw
                new ArgumentOutOfRangeException(
                    string.Format(
                        "User {0} does not have specified role {1}",
                        UserName,
                        role.RoleName));

        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual ICollection<IGroup>
        GetGroups()
        {
            return itsGroupRepository.GetGroupsFor(this);
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
        public virtual IConstraint
        GetConstraint(IRole role)
        {
            if (ConstraintIds == null)
                return new NullConstraint();

            if (!ConstraintIds.ContainsKey(role.RoleName))
                return new NullConstraint();

            return
                itsConstraintRepository.GetConstraint(
                    ConstraintIds[role.RoleName]);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void
        ActivateTenant()
        {
            if (!IsTenantValid)
                throw
                    new InvalidOperationException(
                        "User does not have a valid tenant.");

            Tenant.Activate();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void
        DeactivateTenant()
        {
            if (!IsTenantValid)
                throw 
                    new InvalidOperationException(
                        "User does not have a valid tenant.");

            Tenant.Deactivate();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual bool
        Verify(string verificationCode)
        {
            if (VerificationCode == null)
                return false;

            if (VerificationCode.Equals(verificationCode))
            {
                Status = UserStatus.VERIFIED;
                Notify(new UserVerifiedDomainEvent(this));
                return true;
            }

            return false;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void 
        Initialize(
            ITenantRepository     tenants,
            IGroupRepository      groups,
            IRoleRepository       roles,
            IConstraintRepository constraints)
        {
            itsTenantRepository     = tenants;
            itsGroupRepository      = groups;
            itsRoleRepository       = roles;
            itsConstraintRepository = constraints;

            Tenant = itsTenantRepository.GetUniqueFor(this);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual bool
        IsInRole(IRole role)
        {
            if (role == null)
                return false;

            foreach (IRole candidate in GetRoles())
                if (candidate.Includes(role))
                {
                    IConstraintResult result =
                        GetConstraint(candidate).ApplyTo(this);

                    if (result.IsSatisfied)
                        return true;
                }

            return false;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual bool
        IsInRole(string roleId)
        {
            return IsInRole(itsRoleRepository.GetRole(roleId));
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual bool
        IsInRole(IGroup group,IRole role)
        {
            if (!IsInGroup(group))
                return false;

            foreach (IRole candidate in group.GetRoles())
                if (candidate.Includes(role))
                {
                    IConstraintResult result =
                        GetConstraint(candidate).ApplyTo(this);

                    if (result.IsSatisfied)
                        return true;
                }

            return false;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual bool 
        IsInGroup(IGroup group)
        {
            foreach (IGroup candidate in GetGroups())
                if (candidate.Includes(group))
                    return true;

            return false;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override IUser
        GetSelf()
        {
            return this;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual void 
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
