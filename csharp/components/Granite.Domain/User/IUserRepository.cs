//  ##########################################################################
//  # File Name: IUserRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Persistence.Repository;
using System;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IUserRepository:
        IRepository<Guid,UserIdentity>
    {
    }
}

//  ##########################################################################
