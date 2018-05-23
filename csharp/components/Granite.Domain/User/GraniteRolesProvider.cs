//  ##########################################################################
//  # File Name: GraniteRolesProvider.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Utility;
using System;
using System.Collections.Generic;
using SystemEx.Injection;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class GraniteRolesProvider:
        IProvider<IList<IRole>>
    {
        public IRole GraniteAdministrator { get; protected set; }
        public IRole GraniteUser { get; protected set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        GraniteRolesProvider()
        {
            GraniteUser = 
                new StandardRole("GraniteUser");
            GraniteAdministrator = 
                new StandardRole("GraniteAdministrator",GraniteUser);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public IList<IRole> 
        Get()
        {
            IList<IRole> roles =
                new List<IRole>()
                {
                    GraniteAdministrator,
                    GraniteUser
                };

            roles.AddAll(GetApplicationRoles());
            ValidateRoles(roles);

            return roles;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual ICollection<IRole>
        GetApplicationRoles()
        {
            return new List<IRole>();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected void
        ValidateRoles(IEnumerable<IRole> roles)
        {
            if (roles == null)
                throw new NullReferenceException("No roles were found");

            foreach (IRole role in roles)
                if (!role.Includes(GraniteUser))
                    throw
                        new InvalidOperationException(
                            string.Format(
                                "Role {0} does not include GraniteUser",
                                role.RoleName));
        }
    }
}

//  ##########################################################################
