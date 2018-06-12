//  ##########################################################################
//  # File Name: ITenantDomainEvent.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Domain.Shared;

namespace Granite.Domain.Tenant
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface ITenantDomainEvent:
        IDomainEvent<ITenant>
    {
    }
}

//  ##########################################################################
