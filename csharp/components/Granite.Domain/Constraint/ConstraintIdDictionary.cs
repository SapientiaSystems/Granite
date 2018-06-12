//  ##########################################################################
//  # File Name: ConstraintIdDictionary.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Utility;
using System;
using System.Collections.Generic;

namespace Granite.Domain.Constraint
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class ConstraintIdDictionary:
        MappableDictionary<string,string>
    {
        public
        ConstraintIdDictionary() {}

        public
        ConstraintIdDictionary(IDictionary<string,string> ids)
        {
            if (ids != null)
                foreach (KeyValuePair<string,string> id in ids)
                    Add(id);
        }
    }
}

//  ##########################################################################
