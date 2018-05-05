//  ##########################################################################
//  # File Name: UserIdentity.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class UserIdentity
    {
        public virtual Guid       UserId { get; set; }
        public virtual int        Version { get; set; }

        public virtual string     ExternalUserId { get; set; }
        public virtual UserStatus Status { get; set; }
        public virtual string     VerificationCode { get; set; }
        public virtual DateTime   Created { get; set; }
        public virtual DateTime   LastModified { get; set; }
    }
}

//  ##########################################################################
