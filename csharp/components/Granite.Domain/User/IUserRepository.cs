//  ##########################################################################
//  # File Name: IUserRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.Repository;
using System;
using System.Collections.Generic;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Repository of <c>IUser</c> entities.
    /// </summary>
    ///  
    public
    interface IUserRepository:
        IRepository<Guid,IUser>
    {
        ISet<IUserDomainEventObserver> Observers { get; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Gets a unique <c>IUser</c> entity with the 
        /// provided user id.
        /// </summary>
        /// 
        /// <param name="provider">provides user id</param>
        /// <returns>
        /// <c>IUser</c> associated with provided user id
        /// if it exists, otherwise null.
        /// </returns>
        ///  
        IUser
        GetUniqueFor(IUserIdProvider provider);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Gets a unique <c>IUser</c> entity with the 
        /// specified external user id.
        /// </summary>
        /// 
        /// <param name="externalUserId">external user id</param>
        /// <returns>
        /// <c>IUser</c> associated with specified external user id
        /// if it exists, otherwise null.
        /// </returns>
        ///  
        IUser
        GetUniqueWithExternalUserId(Guid externalUserId);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Gets a unique <c>IUser</c> entity with the 
        /// specified user name.
        /// </summary>
        /// 
        /// <param name="externalUserId">user name</param>
        /// <returns>
        /// <c>IUser</c> associated with specified user name
        /// if it exists, otherwise null.
        /// </returns>
        ///  
        IUser
        GetUniqueWithUserName(string userName);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Determines if  a unique <c>IUser</c> entity is associated
        /// with the specified external user id.
        /// </summary>
        /// 
        /// <param name="externalUserId">external user id</param>
        /// <returns>
        /// true if <c>IUser</c> with associated with specified 
        /// external user id, otherwise false.
        /// </returns>
        ///  
        bool
        HasUniqueWithExternalUserId(Guid externalUserId);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Determines if  a unique <c>IUser</c> entity is associated
        /// with the specified user name.
        /// </summary>
        /// 
        /// <param name="externalUserId">user name</param>
        /// <returns>
        /// true if <c>IUser</c> is associated with specified user name, 
        /// otherwise false.
        /// </returns>
        ///  
        bool
        HasUniqueWithUserName(string userName);
    }
}

//  ##########################################################################
