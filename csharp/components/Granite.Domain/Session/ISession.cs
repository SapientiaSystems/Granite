//  ##########################################################################
//  # File Name: ISession.cs
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
    interface ISession
    {
        Guid     SessionId { get; set; }
        int      Version { get; set; }

        Guid     UserId { get; set; }
        IUser    User { get; }
        bool     IsUserValid { get; }

        string   AccessToken { get; set; }
        string   AccessTokenType { get; set; }
        string   RefreshToken { get; set; }

        DateTime Created { get; set; }
        DateTime LastRefresh { get; set; }
        DateTime LastRequest { get; set; }

        void
        Initialize(IUserRepository userRepository);
    }
}

//  ##########################################################################
