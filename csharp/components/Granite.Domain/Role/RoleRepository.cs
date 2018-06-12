//  ##########################################################################
//  # File Name: RoleRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using SystemEx.Injection;

namespace Granite.Domain.Role
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class RoleRepository:
        IRoleRepository
    {
        private IReadOnlyDictionary<string,IRole> itsRoles;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        RoleRepository(IProvider<IList<IRole>> provider)
        {
            IDictionary<string,IRole> temp = 
                new ConcurrentDictionary<string,IRole>();

            foreach (IRole role in provider.Get())
                temp.Add(role.RoleName,role);

            itsRoles = new ReadOnlyDictionary<string,IRole>(temp);

            if (!HasRole("GraniteUser"))
                throw
                    new InvalidOperationException(
                        "GraniteUser role not found");

            if (!HasRole("GraniteAdministrator"))
                throw
                    new InvalidOperationException(
                        "GraniteAdministrator role not found");
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public IRole 
        GetRole(string roleName)
        {
            if (HasRole(roleName))
                return itsRoles[roleName];

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public ISet<IRole>
        GetRoles(StringSet roleNames)
        {
            return GetRoles(roleNames.ToArray());
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public ISet<IRole>
        GetRoles(string[] roleNames)
        {
            ISet<IRole> roles = new HashSet<IRole>();

            if (roleNames == null)
                return
                    new HashSet<IRole>()
                    {
                        GetRole("GraniteGuest")
                    };

            foreach (string roleName in roleNames)
                if (HasRole(roleName))
                    roles.Add(itsRoles[roleName]);
           
            return roles;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public ISet<IRole>
        GetRoles()
        {
            return new HashSet<IRole>(itsRoles.Values);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public bool 
        HasRole(string roleName)
        {
            return itsRoles.ContainsKey(roleName);
        }
    }
}

//  ##########################################################################
