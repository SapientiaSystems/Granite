//  ##########################################################################
//  # File Name: ITenantDomainEventObserver.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Domain.Shared;
using System;

namespace Granite.Domain.Tenant
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface ITenantDomainEventObserver:
        IDomainEventObserver<ITenantDomainEvent>
    {
    }
}

//  ##########################################################################
