//  ##########################################################################
//  # File Name: IMultiTenantEntity.cs
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
    public
    interface IMultiTenantEntity<K>:
        IEntity<K>
    {
        MultiTenantId<K> MultiTenantPrimaryId { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        SetTenantId(Guid tenantId);
    }
}

//  ##########################################################################
