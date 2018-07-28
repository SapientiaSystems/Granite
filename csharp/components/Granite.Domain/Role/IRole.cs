//  ##########################################################################
//  # File Name: IRole.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Utility;
using System.Collections.Generic;

namespace Granite.Domain.Role
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IRole:
        ICopyable
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
