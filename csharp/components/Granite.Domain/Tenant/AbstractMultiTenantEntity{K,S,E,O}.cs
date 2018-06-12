//  ##########################################################################
//  # File Name: AbstractMultiTenantEntity.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.Shared;
using System;

namespace Granite.Domain.Tenant
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public abstract
    class AbstractMultiTenantEntity<K,S,E,O>:
        AbstractEntity<K,S,E,O>,
        IMultiTenantEntity<K>
        where S: IDomainEventSource<S,E,O>
        where E: IDomainEvent<S>
        where O: IDomainEventObserver<E>
    {
        public virtual MultiTenantId<K> MultiTenantPrimaryId { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void
        SetTenantId(Guid tenantId)
        {
            MultiTenantPrimaryId = new MultiTenantId<K>(tenantId,PrimaryId);
        }
    }

}

//  ##########################################################################
