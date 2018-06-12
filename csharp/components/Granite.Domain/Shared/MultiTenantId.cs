//  ##########################################################################
//  # File Name: MultiTenantId.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Utility;
using System;

namespace Granite.Domain.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class MultiTenantId<K>:
        IEquatable<MultiTenantId<K>>
    {
        public virtual Guid TenantId { get; set; }
        public virtual K    EntityId { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        MultiTenantId()
        {
            TenantId = Guid.Empty;
            EntityId = default(K);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        MultiTenantId(Guid tenantId,K entityId)
        {
            TenantId = tenantId;
            EntityId = entityId;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual bool
        Equals(MultiTenantId<K> other)
        {
            if (other == null)
                return false;

            if (ReferenceEquals(this,other))
                return true;

            return
                TenantId.Equals(other.TenantId) &&
                EntityId.Equals(other.EntityId);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override bool
        Equals(Object other)
        {
            return
                other is MultiTenantId<K> &&
                Equals((MultiTenantId<K>)other);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override int 
        GetHashCode()
        {
            return
                new HashCodeBuilder()
                    .Append(TenantId.GetHashCode())
                    .Append(EntityId.GetHashCode())
                    .HashCode;
        }
    }
}

//  ##########################################################################
