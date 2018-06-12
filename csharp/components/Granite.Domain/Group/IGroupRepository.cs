//  ##########################################################################
//  # File Name: IGroupRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Granite.Domain.Tenant;
using Strata.Domain.Repository;
using System;
using System.Collections.Generic;

namespace Granite.Domain.Group
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IGroupRepository:
        IMultiTenantRepository<string,IGroup>
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ICollection<IGroup>
        GetGroupsFor(IUserIdProvider user);
    }
}

//  ##########################################################################
