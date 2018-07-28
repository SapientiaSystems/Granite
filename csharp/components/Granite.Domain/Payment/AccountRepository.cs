//  ##########################################################################
//  # File Name: AccountRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.NamedQuery;
using Strata.Domain.Repository;
using Strata.Domain.UnitOfWork;
using System;
using System.Collections.Generic;
using SystemEx.Injection;

namespace Granite.Domain.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class AccountRepository:
        AbstractRepository<
            Guid,
            IAccount,
            IAccountDomainEvent,
            IAccountDomainEventObserver>,
        IAccountRepository
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        AccountRepository(IUnitOfWorkProvider provider):
            base(provider) {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public ICollection<IAccount>
        GetAllFor(IUserIdProvider provider)
        {
            INamedQuery<IAccount> query =
                GetNamedQuery("Account.GetAllFor");

            query.SetInput("userId",provider.UserId);
            return query.GetAll();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public IAccount 
        GetUniqueFor(IUserIdProvider provider)
        {
            INamedQuery<IAccount> query =
                GetNamedQuery("Account.GetUniqueFor");

            query.SetInput("userId",provider.UserId);
            return query.GetUnique();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public bool
        HasAnyFor(IUserIdProvider provider)
        {
            INamedQuery<IAccount> query =
                GetNamedQuery("Account.HasAnyFor");

            query.SetInput("userId",provider.UserId);
            return query.HasAny();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public bool 
        HasUniqueFor(IUserIdProvider provider)
        {
            INamedQuery<IAccount> query =
                GetNamedQuery("Account.HasUniqueFor");

            query.SetInput("userId",provider.UserId);
            return query.HasUnique();
        }
    }
}

//  ##########################################################################
