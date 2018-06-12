//  ##########################################################################
//  # File Name: IUserFactory.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Role;
using Granite.Domain.Tenant;
using Strata.Foundation.Value;
using System.Collections.Generic;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IUserFactory
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IUser
        CreateUser(
            ITenant tenant,
            string userName,
            HashedString password,
            EmailAddress email,
            ISet<IRole>  roles);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IUser
        CreateUser(
            ITenant      tenant,
            string       userName,
            HashedString password,
            EmailAddress email,
            string       subscriptionType);
    }
}

//  ##########################################################################
