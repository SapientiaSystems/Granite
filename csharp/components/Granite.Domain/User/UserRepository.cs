//  ##########################################################################
//  # File Name: UserRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Persistence.Repository;
using Strata.Persistence.UnitOfWork;
using System;
using SystemEx.Injection;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class UserRepository:
        AbstractRepository<Guid,UserIdentity>,
        IUserRepository
    {
        [Inject]
        public
        UserRepository(IUnitOfWorkProvider provider) :
            base(provider) {}
    }
}

//  ##########################################################################
