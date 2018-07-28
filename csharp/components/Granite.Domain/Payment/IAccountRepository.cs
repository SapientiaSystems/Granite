//  ##########################################################################
//  # File Name: IAccountRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.Repository;
using System;
using System.Collections.Generic;

namespace Granite.Domain.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IAccountRepository:
        IRepository<
            Guid,
            IAccount,
            IAccountDomainEvent,
            IAccountDomainEventObserver>
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ICollection<IAccount>
        GetAllFor(IUserIdProvider provider);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IAccount
        GetUniqueFor(IUserIdProvider provider);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        HasAnyFor(IUserIdProvider provider);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        HasUniqueFor(IUserIdProvider provider);
    }
}

//  ##########################################################################
