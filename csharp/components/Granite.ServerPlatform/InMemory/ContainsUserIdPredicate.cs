//  ##########################################################################
//  # File Name: ContainsUserIdPredicate.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Group;
using Strata.Domain.InMemory;
using System;
using System.Collections.Generic;

namespace Granite.ServerPlatform.InMemory
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class ContainsUserIdPredicate:
        IPredicate<IGroup>
    {
        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public bool 
        Evaluate(IGroup target,IDictionary<string,object> inputs)
        {
            Guid userId = (Guid)inputs["userId"];

            return target.MemberUserIds.Contains(userId);
        }
    }
}

//  ##########################################################################
