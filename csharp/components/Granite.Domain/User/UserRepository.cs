//  ##########################################################################
//  # File Name: UserRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Persistence.NamedQuery;
using Strata.Persistence.Repository;
using Strata.Persistence.UnitOfWork;
using System;
using SystemEx.Injection;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class UserRepository:
        AbstractRepository<Guid,IUser>,
        IUserRepository
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        UserRepository(IUnitOfWorkProvider provider) :
            base(provider) {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public IUser 
        GetUniqueWithExternalUserId(Guid externalUserId)
        {
            INamedQuery<IUser> query = 
                GetNamedQuery("User.GetUniqueWithExternalUserId");

            query.SetInput("externalUserId",externalUserId);
            return query.GetUnique();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public IUser 
        GetUniqueWithUserName(string userName)
        {
            INamedQuery<IUser> query =
                GetNamedQuery("User.GetUniqueWithUserName");

            query.SetInput("userName",userName);
            return query.GetUnique();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public bool 
        HasUniqueWithExternalUserId(Guid externalUserId)
        {
            INamedQuery<IUser> query =
                GetNamedQuery("User.HasUniqueWithExternalUserId");

            query.SetInput("externalUserId",externalUserId);
            return query.HasUnique();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public bool 
        HasUniqueWithUserName(string userName)
        {
            INamedQuery<IUser> query =
                GetNamedQuery("User.GetUniqueWithUserName");

            query.SetInput("userName",userName);
            return query.HasUnique();
        }
    }
}

//  ##########################################################################
