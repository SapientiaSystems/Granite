//  ##########################################################################
//  # File Name: TenantDeactivatedDomainEvent.cs
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
    class TenantDeactivatedDomainEvent:
        AbstractDomainEvent<ITenant>,
        ITenantDomainEvent
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        TenantDeactivatedDomainEvent(ITenant source):
            base("TenantDeactivated",source) {}
    }
}

//  ##########################################################################
