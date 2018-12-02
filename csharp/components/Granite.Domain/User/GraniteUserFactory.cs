//  ##########################################################################
//  # File Name: GraniteUserFactory.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Verification;
using Granite.Domain.Constraint;
using Granite.Domain.Group;
using Granite.Domain.Role;
using Granite.Domain.Tenant;
using Strata.Foundation.Value;
using System.Collections.Generic;
using SystemEx.Injection;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class GraniteUserFactory:
        IUserFactory
    {
        protected ITenantRepository          Tenants { get; set; }
        protected IGroupRepository           Groups { get; set; }
        protected IRoleRepository            Roles { get; set; }
        protected IConstraintRepository      Constraints { get; set; }
        protected IVerificationCodeGenerator Generator { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        GraniteUserFactory(
            ITenantRepository          tenants,
            IGroupRepository           groups,
            IRoleRepository            roles,
            IConstraintRepository      constraints,
            IVerificationCodeGenerator generator)
        {
            Tenants     = tenants;
            Groups      = groups;
            Roles       = roles;
            Constraints = constraints;
            Generator   = generator;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual IUser 
        CreateUser(
            ITenant      tenant,
            string       userName,
            HashedString password,
            EmailAddress email,
            ISet<IRole>  roles)
        {
            IUser user =
                new StandardUser(tenant,Tenants,Groups,Roles,Constraints)
                {
                    TenantId         = tenant.TenantId,
                    UserName         = userName,
                    Password         = password,
                    Email            = email,
                    Status           = UserStatus.PENDING,
                    VerificationCode = Generator.Generate()
                };

            user.AttachRoles(roles);
            return user;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual IUser 
        CreateUser(
            ITenant      tenant,
            string       userName,
            HashedString password,
            EmailAddress email,
            string       subscriptionType)
        {
            IUser user =
                new StandardUser(tenant,Tenants,Groups,Roles,Constraints)
                {
                    TenantId         = tenant.TenantId,
                    UserName         = userName,
                    Password         = password,
                    Email            = email,
                    Status           = UserStatus.PENDING,
                    VerificationCode = Generator.Generate()
                };

            user.AttachRoles(GetRolesFor(subscriptionType));
            SetConstraints(user,subscriptionType);
            return user;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual ISet<IRole> 
        GetRolesFor(string subscriptionType)
        {
            return
                new HashSet<IRole>()
                {
                    Roles.GetRole("GraniteUser")
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual void 
        SetConstraints(IUser user,string subscriptionType)
        {
        }

    }
}

//  ##########################################################################
