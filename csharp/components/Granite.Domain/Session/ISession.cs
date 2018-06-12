//  ##########################################################################
//  # File Name: ISession.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Granite.Domain.User;
using Strata.Domain.Shared;
using System;

namespace Granite.Domain.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface ISession:
        IEntity<Guid>,
        IDomainEventSource<
            ISession,
            ISessionDomainEvent,
            ISessionDomainEventObserver>,
        IUserIdProvider
    {
        new Guid UserId { get; set; }

        IUser    User { get; }
        bool     IsUserValid { get; }
        bool     IsOpen { get; }

        string   AccessToken { get; set; }

        DateTime LastRefresh { get; set; }
        DateTime LastRequest { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        ActivateTenant();

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        DeactivateTenant();

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        Open();

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        Refresh();

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        Close();

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        Initialize(IUserRepository userRepository);
    }
}

//  ##########################################################################
