//  ##########################################################################
//  # File Name: IMultiTenantRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.Repository;
using System;

namespace Granite.Domain.Tenant
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IMultiTenantRepository<K,E>:
        IRepository<MultiTenantId<K>,E>,
        ITenantDomainEventObserver
        where E: class,IMultiTenantEntity<K>
    {
        Guid ActiveTenantId { get; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        E
        GetUnique(K key);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        HasUnique(K key);
        
    }
}

//  ##########################################################################
