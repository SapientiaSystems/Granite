//  ##########################################################################
//  # File Name: StandardSession.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Granite.Domain.User;
using System;

namespace Granite.Domain.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class StandardSession:
        ISession
    {
        public virtual Guid     SessionId { get; set; }
        public virtual int      Version { get; set; }

        public virtual Guid     UserId { get; set; }
        public virtual IUser    User { get; protected set; }
        public virtual bool     IsUserValid { get { return User != null; } }

        public virtual string   AccessToken { get; set; }
        public virtual string   AccessTokenType { get; set; }
        public virtual string   RefreshToken { get; set; }

        public virtual DateTime Created { get; set; }
        public virtual DateTime LastRefresh { get; set; }
        public virtual DateTime LastRequest { get; set; }

        public
        StandardSession()
        {
            User = null;
        }

        public virtual void
        Initialize(IUserRepository userRepository)
        {
            User = userRepository.GetUnique(UserId);
        }
    }
}

//  ##########################################################################
