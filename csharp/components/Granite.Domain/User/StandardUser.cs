//  ##########################################################################
//  # File Name: StandardUser.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
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
        IUser
    {
        public virtual Guid         UserId { get; set; }
        public virtual int          Version { get; set; }

        public virtual string       ExternalUserId { get; set; }
        public virtual string       UserName { get; set; }
        public virtual EmailAddress Email { get; set; }
        public virtual string       JobTitle { get; set; }
        public virtual IReadOnlyCollection<IRole> 
                                    Roles { get; protected set; }

        public virtual UserStatus   Status { get; set; }
        public virtual string       VerificationCode { get; set; }

        public virtual DateTime     Created { get; set; }
        public virtual DateTime     LastModified { get; set; }

        protected virtual RoleSet   RoleNames { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void
        SetRoles(ISet<IRole> roles)
        {
            Roles = new HashSet<IRole>(roles);
            RoleNames = new RoleSet();

            foreach (IRole role in Roles)
                RoleNames.Add(role.RoleName);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void 
        InitializeRoles(IRoleRepository roleRepository)
        {
            if (RoleNames != null)
            {
                HashSet<IRole> roles = new HashSet<IRole>();

                foreach (string roleName in RoleNames)
                    if (roleRepository.HasRole(roleName))
                        roles.Add(roleRepository.GetRole(roleName));

                Roles = roles;
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual bool 
        IsInRole(IRole role)
        {
            foreach (IRole authorizedRole in Roles)
                if (authorizedRole.Includes(role))
                    return true;

            return false;
        }
    }
}

//  ##########################################################################
