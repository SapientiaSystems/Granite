//  ##########################################################################
//  # File Name: TenantReplicator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Tenant;
using Strata.Domain.InMemory;
using Strata.Foundation.Utility;
using System;

namespace Granite.ServerPlatform.InMemory
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class TenantReplicator:
        AbstractEntityReplicator<Guid,ITenant>
    {
        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        protected override ITenant 
        MakeCopy(ITenant original)
        {
            return original.Copy();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        protected override bool 
        HasUnassignedKey(ITenant entity)
        {
            return entity.PrimaryId == Guid.Empty;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        protected override void 
        AssignKey(ITenant entity,Guid key)
        {
            entity.PrimaryId = key;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        protected override Guid 
        GenerateKey()
        {
            return Guid.NewGuid();
        }
    }
}

//  ##########################################################################
