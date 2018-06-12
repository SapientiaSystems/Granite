//  ##########################################################################
//  # File Name: ITenant.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Granite.Domain.User;
using Strata.Domain.Shared;
using Strata.Foundation.Value;
using System;

namespace Granite.Domain.Tenant
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface ITenant:
        IEntity<Guid>,
        ITenantIdProvider,
        IDomainEventSource<
            ITenant,
            ITenantDomainEvent,
            ITenantDomainEventObserver>
    {
        string       TenantName { get; set; }
        HashedString SecretKey { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ITenant
        Activate();

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ITenant
        Deactivate();
    }
}

//  ##########################################################################
