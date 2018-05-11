//  ##########################################################################
//  # File Name: ExternalUserIdEqualsPredicate.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.User;
using Strata.Persistence.InMemory;
using System;
using System.Collections.Generic;

namespace Granite.ServerPlatform.InMemory
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class ExternalUserIdEqualsPredicate:
        IPredicate<IUser>
    {
        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public bool 
        Evaluate(IUser target,IDictionary<string,object> inputs)
        {
            Guid externalUserId = (Guid)inputs["externalUserId"];

            return target.ExternalUserId.Equals(externalUserId);
        }
    }
}

//  ##########################################################################
