//  ##########################################################################
//  # File Name: ISubscription.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.Shared;
using System;

namespace Granite.Domain.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface ISubscription:
        IEntity<Guid>,
        ISubscriptionIdProvider,
        IAccountIdProvider,
        IPlanIdProvider,
        IUserIdProvider,
        IDomainEventSource<
            ISubscription,
            ISubscriptionDomainEvent,
            ISubscriptionDomainEventObserver>
    {
        new Guid    SubscriptionId { get; set; }
        new Guid    AccountId { get; set; }
        new Guid    PlanId { get; set; }
        new Guid    UserId { get; set; }

        string      ExternalSubscriptionId { get; set; }
        string      ExternalPlanId { get; set; }

        DateTime    InitialPaymentDate { get; set; }
    }
}

//  ##########################################################################
