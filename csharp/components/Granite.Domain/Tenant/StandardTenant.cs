//  ##########################################################################
//  # File Name: StandardTenant.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Domain.Shared;
using Strata.Foundation.Utility;
using Strata.Foundation.Value;
using System;
using System.Collections.Generic;

namespace Granite.Domain.Tenant
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class StandardTenant:
        AbstractEntity<
            Guid,
            ITenant,
            ITenantDomainEvent,
            ITenantDomainEventObserver>,
        ITenant
    {
        public virtual Guid         TenantId { get { return PrimaryId; } }
        public virtual string       TenantName { get; set; }
        public virtual HashedString SecretKey { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        StandardTenant() {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        StandardTenant(StandardTenant other)
        {
            Observers = 
                new HashSet<ITenantDomainEventObserver>(other.Observers);

            PrimaryId = other.PrimaryId;
            Version = other.Version;
            Created = other.Created;
            LastModified = other.LastModified;

            TenantName = other.TenantName;
            SecretKey = other.SecretKey;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        StandardTenant(string name,HashedString key)
        {
            TenantName = name;
            SecretKey  = key;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual ICopyable
        MakeCopy()
        {
            return new StandardTenant(this);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual ITenant 
        Activate()
        {
            Notify(new TenantActivatedDomainEvent(this));
            return this;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual ITenant 
        Deactivate()
        {
            Notify(new TenantDeactivatedDomainEvent(this));
            return this;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override ITenant
        GetSelf()
        {
            return this;
        }
    }
}

//  ##########################################################################
