//  ##########################################################################
//  # File Name: RoleReplicator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Role;
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
    class RoleReplicator:
        AbstractEntityReplicator<string,IRole>
    {
        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        protected override IRole
        MakeCopy(IRole original)
        {
            return original.Copy();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        protected override bool 
        HasUnassignedKey(IRole entity)
        {
            return string.IsNullOrEmpty(entity.RoleName);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        protected override void 
        AssignKey(IRole entity,string key)
        {
            throw new InvalidOperationException();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        protected override string 
        GenerateKey()
        {
            throw new InvalidOperationException();
        }
    }
}

//  ##########################################################################
