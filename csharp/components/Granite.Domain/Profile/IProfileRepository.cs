//  ##########################################################################
//  # File Name: IProfileRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Granite.Domain.User;
using Strata.Domain.Repository;
using System;

namespace Granite.Domain.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IProfileRepository:
        IRepository<Guid,IProfile>
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IProfile
        GetUniqueFor(IUserIdProvider provider);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IProfile
        GetUniqueWithUserName(string userName);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        HasUniqueFor(IUserIdProvider provider);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        HasUniqueWithUserName(string userName);
    }
}

//  ##########################################################################
