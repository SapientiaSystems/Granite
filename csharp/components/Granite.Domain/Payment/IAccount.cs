//  ##########################################################################
//  # File Name: IAccount.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.Shared;
using System;

namespace Granite.Domain.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IAccount:
        IEntity<Guid>,
        IAccountIdProvider,
        ITenantIdProvider,
        IUserIdProvider,
        IDomainEventSource<
            IAccount,
            IAccountDomainEvent,
            IAccountDomainEventObserver>
    {
        new Guid AccountId { get; set; }
        new Guid TenantId { get; set; }
        new Guid UserId { get; set; }

        string   ExternalAccountId { get; set; }
        string   ExternalPaymentMethodId { get; set; }
    }
}

//  ##########################################################################
