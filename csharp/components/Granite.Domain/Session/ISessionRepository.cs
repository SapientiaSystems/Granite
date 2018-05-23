//  ##########################################################################
//  # File Name: ISessionRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Persistence.Repository;
using System;
using System.Collections.Generic;

namespace Granite.Domain.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface ISessionRepository:
        IRepository<Guid,ISession>
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ICollection<ISession>
        GetAllWithUserId(Guid userId);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        HasAnyWithUserId(Guid userId);
    }
}

//  ##########################################################################
