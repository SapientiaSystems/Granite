//  ##########################################################################
//  # File Name: StandardUser.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Value;
using System;

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
        public virtual UserStatus   Status { get; set; }
        public virtual string       VerificationCode { get; set; }
        public virtual DateTime     Created { get; set; }
        public virtual DateTime     LastModified { get; set; }
    }
}

//  ##########################################################################
