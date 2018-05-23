//  ##########################################################################
//  # File Name: UserRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Persistence.NamedQuery;
using Strata.Persistence.Repository;
using Strata.Persistence.UnitOfWork;
using System;
using System.Collections.Generic;
using System.Linq.Expressions;
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
        private IRoleRepository itsRoleRepository;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        UserRepository(
            IUnitOfWorkProvider provider,
            IRoleRepository     roleRepository):
            base(provider)
        {
            itsRoleRepository = roleRepository;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IUser
        GetUnique(Guid userId)
        {
            IUser result = base.GetUnique(userId);

            if (result != null)
                result.InitializeRoles(itsRoleRepository);

            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IUser
        GetUnique(Expression<Func<IUser,bool>> predicate)
        {
            IUser result = base.GetUnique(predicate);

            if (result != null)
                result.InitializeRoles(itsRoleRepository);

            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IList<IUser>
        GetAll()
        {
            IList<IUser> results = base.GetAll();

            foreach (IUser result in results)
                if (result != null)
                    result.InitializeRoles(itsRoleRepository);

            return results;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IList<IUser>
        GetAll(Expression<Func<IUser,bool>> predicate)
        {
            IList<IUser> results = base.GetAll(predicate);

            foreach (IUser result in results)
                if (result != null)
                    result.InitializeRoles(itsRoleRepository);

            return results;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public IUser 
        GetUniqueWithExternalUserId(Guid externalUserId)
        {
            INamedQuery<IUser> query = 
                GetNamedQuery("User.GetUniqueWithExternalUserId");
            IUser result = null;

            query.SetInput("externalUserId",externalUserId);
            result = query.GetUnique();

            if (result != null)
                result.InitializeRoles(itsRoleRepository);

            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public IUser 
        GetUniqueWithUserName(string userName)
        {
            INamedQuery<IUser> query =
                GetNamedQuery("User.GetUniqueWithUserName");
            IUser result = null;

            query.SetInput("userName",userName);
            result = query.GetUnique();

            if (result != null)
                result.InitializeRoles(itsRoleRepository);

            return result;
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
                GetNamedQuery("User.HasUniqueWithUserName");

            query.SetInput("userName",userName);
            return query.HasUnique();
        }
    }
}

//  ##########################################################################
