//  ##########################################################################
//  # File Name: ITenantRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.Repository;
using System;
using System.Collections.Generic;

namespace Granite.Domain.Tenant
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface ITenantRepository:
        IRepository<Guid,ITenant>
    {
        ISet<ITenantDomainEventObserver> Observers { get; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ITenant
        GetUniqueFor(ITenantIdProvider provider);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ITenant
        GetUniqueWithTenantName(string tenantName);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        HasUniqueFor(ITenantIdProvider provider);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        HasUniqueWithTenantName(string tenantName);
    }
}

//  ##########################################################################
