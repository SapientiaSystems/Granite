//  ##########################################################################
//  # File Name: StringSet.cs
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
    class StringSet:
        MappableSet<string>
    {
        public
        StringSet() {}

        public
        StringSet(ICollection<string> roles)
        {
            if (roles != null)
                foreach (string role in roles)
                    Add(role);
        }
    }
}

//  ##########################################################################
