//  ##########################################################################
//  # File Name: IRole.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.Collections.Generic;

namespace Granite.Domain.Role
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IRole
    {
        string                     RoleName { get; }
        IReadOnlyCollection<IRole> IncludedRoles { get; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        Includes(IRole role);
    }
}

//  ##########################################################################
