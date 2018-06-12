//  ##########################################################################
//  # File Name: IRoleRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using System.Collections.Generic;

namespace Granite.Domain.Role
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IRoleRepository
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IRole
        GetRole(string roleName);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ISet<IRole>
        GetRoles(StringSet roleNames);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ISet<IRole>
        GetRoles(string[] roleNames);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ISet<IRole>
        GetRoles();

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        HasRole(string roleName);
    }
}

//  ##########################################################################
