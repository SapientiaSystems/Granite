//  ##########################################################################
//  # File Name: GroupIdSet.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Utility;
using System.Collections.Generic;

namespace Granite.Domain.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class GroupIdSet:
        MappableSet<MultiTenantId<string>>
    {
        public
        GroupIdSet() {}

        public
        GroupIdSet(ICollection<MultiTenantId<string>> ids)
        {
            if (ids != null)
                foreach (MultiTenantId<string> id in ids)
                    Add(id);
        }
    }
}

//  ##########################################################################
