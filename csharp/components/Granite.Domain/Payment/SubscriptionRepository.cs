//  ##########################################################################
//  # File Name: SubscriptionRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.NamedQuery;
using Strata.Domain.Repository;
using Strata.Domain.UnitOfWork;
using System;
using System.Collections.Generic;
using SystemEx.Injection;

namespace Granite.Domain.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class SubscriptionRepository:
        AbstractRepository<
            Guid,
            ISubscription,
            ISubscriptionDomainEvent,
            ISubscriptionDomainEventObserver>,
        ISubscriptionRepository
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        SubscriptionRepository(IUnitOfWorkProvider provider) :
            base(provider) {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public ICollection<ISubscription>
        GetAllFor(IAccountIdProvider provider)
        {
            INamedQuery<ISubscription> query =
                GetNamedQuery("Subscription.GetAllWithAccountId");

            query.SetInput("accountId",provider.AccountId);
            return query.GetAll();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public ICollection<ISubscription>
        GetAllFor(IUserIdProvider provider)
        {
            INamedQuery<ISubscription> query =
                GetNamedQuery("Subscription.GetAllWithUserId");

            query.SetInput("userId",provider.UserId);
            return query.GetAll();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public ISubscription 
        GetUniqueFor(IAccountIdProvider provider)
        {
            INamedQuery<ISubscription> query =
                GetNamedQuery("Subscription.GetUniqueWithAccountId");

            query.SetInput("accountId",provider.AccountId);
            return query.GetUnique();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public ISubscription
        GetUniqueFor(IUserIdProvider provider)
        {
            INamedQuery<ISubscription> query =
                GetNamedQuery("Subscription.GetUniqueWithUserId");

            query.SetInput("userId",provider.UserId);
            return query.GetUnique();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public bool 
        HasAnyFor(IAccountIdProvider provider)
        {
            INamedQuery<ISubscription> query =
                GetNamedQuery("Subscription.HasAnyWithAccountId");

            query.SetInput("accountId",provider.AccountId);
            return query.HasAny();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public bool 
        HasAnyFor(IUserIdProvider provider)
        {
            INamedQuery<ISubscription> query =
                GetNamedQuery("Subscription.HasAnyWithUserId");

            query.SetInput("userId",provider.UserId);
            return query.HasAny();
        }
    }
}

//  ##########################################################################
