//  ##########################################################################
//  # File Name: GuidSet.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Utility;
using System;
using System.Collections.Generic;

namespace Granite.Domain.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class GuidSet:
        MappableSet<Guid>
    {
        public
        GuidSet() {}

        public
        GuidSet(ICollection<Guid> guids)
        {
            if (guids != null)
                foreach (Guid guid in guids)
                    Add(guid);
        }
    }
}

//  ##########################################################################
