//  ##########################################################################
//  # File Name: IUser.cs
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
    /// Represents user account information.
    /// </summary>
    ///  
    public
    interface IUser
    {
        Guid                       UserId { get; set; }
        int                        Version { get; set; }

        string                     ExternalUserId { get; set; }
        string                     UserName { get; set; }
        EmailAddress               Email { get; set; }
        string                     JobTitle { get; set; }
        IReadOnlyCollection<IRole> Roles { get; }

        UserStatus                 Status { get; set; }
        string                     VerificationCode { get; set; }

        DateTime                   Created { get; set; }
        DateTime                   LastModified { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        SetRoles(ISet<IRole> roles);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        InitializeRoles(IRoleRepository roleRepository);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        IsInRole(IRole role);
    }
}

//  ##########################################################################
