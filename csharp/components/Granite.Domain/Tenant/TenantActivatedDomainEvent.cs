//  ##########################################################################
//  # File Name: TenantActivatedDomainEvent.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Domain.Shared;

namespace Granite.Domain.Tenant
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class TenantActivatedDomainEvent:
        AbstractDomainEvent<ITenant>,
        ITenantDomainEvent
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        TenantActivatedDomainEvent(ITenant source):
            base("TenantActivated",source) {}
    }
}

//  ##########################################################################
