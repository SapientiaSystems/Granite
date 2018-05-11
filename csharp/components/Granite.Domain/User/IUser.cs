//  ##########################################################################
//  # File Name: IUser.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Value;
using System;

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
        Guid         UserId { get; set; }
        int          Version { get; set; }

        string       ExternalUserId { get; set; }
        string       UserName { get; set; }
        EmailAddress Email { get; set; }
        string       JobTitle { get; set; }
        UserStatus   Status { get; set; }
        string       VerificationCode { get; set; }
        DateTime     Created { get; set; }
        DateTime     LastModified { get; set; }
    }
}

//  ##########################################################################
