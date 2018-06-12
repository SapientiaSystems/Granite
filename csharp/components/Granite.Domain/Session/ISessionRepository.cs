//  ##########################################################################
//  # File Name: ISessionRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Domain.Repository;
using System;
using System.Collections.Generic;

namespace Granite.Domain.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface ISessionRepository:
        IRepository<
            Guid,
            ISession,
            ISessionDomainEvent,
            ISessionDomainEventObserver>
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ISession
        GetUniqueWithAccessToken(string accessToken);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ICollection<ISession>
        GetAllWithUserId(Guid userId);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        HasUniqueWithAccessToken(string accessToken);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        HasAnyWithUserId(Guid userId);
    }
}

//  ##########################################################################
