//  ##########################################################################
//  # File Name: GranitePrincipal.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Role;
using Granite.Domain.User;
using System.Collections.Generic;
using System.Linq;
using System.Security.Principal;

namespace Granite.ServerPlatform.WebApi
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class GranitePrincipal:
        GenericPrincipal,
        IPrincipal
    {
        public IUser User { get; protected set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        GranitePrincipal(IUser user):
            base(new GenericIdentity(user.UserName),GetRoleIds(user))
        {
            User = user;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public override bool
        IsInRole(string roleId)
        {
            return User.IsInRole(roleId);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected static string[]
        GetRoleIds(IUser user)
        {
            ISet<string> roleIds = new HashSet<string>();

            foreach (IRole role in user.GetRoles())
                roleIds.Add(role.RoleName);

            return roleIds.ToArray();
        }

    }
}

//  ##########################################################################
