//  ##########################################################################
//  # File Name: IUser.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Constraint;
using Granite.Domain.Group;
using Granite.Domain.Role;
using Granite.Domain.Shared;
using Granite.Domain.Tenant;
using Strata.Domain.Shared;
using Strata.Foundation.Value;
using System;
using System.Collections.Generic;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Represents user account information.
    /// </summary>
    ///  
    public
    interface IUser:
        IEntity<Guid>,
        IUserIdProvider,
        ITenantIdProvider,
        IDomainEventSource<
            IUser,
            IUserDomainEvent,
            IUserDomainEventObserver>
    {
        new Guid                    UserId { get; set; }
        new Guid                    TenantId { get; set; }

        bool                        IsTenantValid { get; }
        ITenant                     Tenant { get; }

        string                      UserName { get; set; }
        HashedString                Password { get; set; }

        EmailAddress                Email { get; set; }
        string                      ExternalUserId { get; set; }

        UserStatus                  Status { get; set; }
        string                      VerificationCode { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IUser
        AttachRole(IRole role);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IUser
        AttachRoles(ISet<IRole> roles);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IUser
        SetConstraint(IRole role,IConstraint constraint);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ICollection<IGroup>
        GetGroups();

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
        IConstraint
        GetConstraint(IRole role);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        ActivateTenant();

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        DeactivateTenant();

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        Verify(string verificationCode);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        IsInRole(IRole role);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        IsInRole(string roleId);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        IsInRole(IGroup group,IRole role);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        IsInGroup(IGroup group);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        Initialize(
            ITenantRepository tenants,
            IGroupRepository groups,
            IRoleRepository roles,
            IConstraintRepository constraints);
    }
}

//  ##########################################################################
