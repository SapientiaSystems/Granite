//  ##########################################################################
//  # File Name: UserNameEqualsPredicate.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.User;
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
    class UserNameEqualsPredicate:
        IPredicate<IUser>
    {
        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public bool 
        Evaluate(IUser target,IDictionary<string,object> inputs)
        {
            string userName = (string)inputs["userName"];

            return target.UserName.Equals(userName);
        }
    }
}

//  ##########################################################################
