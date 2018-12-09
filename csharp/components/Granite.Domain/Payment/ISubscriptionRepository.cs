//  ##########################################################################
//  # File Name: ISubscriptionRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.Repository;
using System;
using System.Collections.Generic;

namespace Granite.Domain.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface ISubscriptionRepository:
        IRepository<
            Guid,
            ISubscription,
            ISubscriptionDomainEvent,
            ISubscriptionDomainEventObserver>
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ICollection<ISubscription>
        GetAllFor(IAccountIdProvider provider);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ICollection<ISubscription>
        GetAllFor(IUserIdProvider provider);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ISubscription
        GetUniqueFor(IAccountIdProvider provider);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ISubscription
        GetUniqueFor(IUserIdProvider provider);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        HasAnyFor(IAccountIdProvider provider);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        HasAnyFor(IUserIdProvider provider);
    }
}

//  ##########################################################################
