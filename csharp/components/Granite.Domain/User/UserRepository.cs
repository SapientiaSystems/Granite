//  ##########################################################################
//  # File Name: UserRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Constraint;
using Granite.Domain.Group;
using Granite.Domain.Role;
using Granite.Domain.Shared;
using Granite.Domain.Tenant;
using Strata.Domain.NamedQuery;
using Strata.Domain.Repository;
using Strata.Domain.UnitOfWork;
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
        public ISet<IUserDomainEventObserver> 
        Observers { get; protected set; }

        private ITenantRepository     itsTenantRepository;
        private IGroupRepository      itsGroupRepository;
        private IRoleRepository       itsRoleRepository;
        private IConstraintRepository itsConstraintRepository;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        UserRepository(
            IUnitOfWorkProvider   provider,
            ITenantRepository     tenantRepository,
            IGroupRepository      groupRepository,
            IRoleRepository       roleRepository,
            IConstraintRepository constraintRepository):
            base(provider)
        {
            Observers = new HashSet<IUserDomainEventObserver>();

            itsTenantRepository     = tenantRepository;
            itsGroupRepository      = groupRepository;
            itsRoleRepository       = roleRepository;
            itsConstraintRepository = constraintRepository;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IUser
        Insert(IUser user)
        {
            IUser result = base.Insert(user);

            if (result != null)
            {
                result.Initialize(
                    itsTenantRepository,
                    itsGroupRepository,
                    itsRoleRepository,
                    itsConstraintRepository);
                result.Attach(Observers);
            }

            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IUser
        Update(IUser user)
        {
            IUser result = base.Update(user);

            if (result != null)
            {
                result.Initialize(
                    itsTenantRepository,
                    itsGroupRepository,
                    itsRoleRepository,
                    itsConstraintRepository);
                result.Attach(Observers);
            }

            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IUser
        GetUnique(Guid userId)
        {
            IUser result = base.GetUnique(userId);

            if (result != null)
            {
                result.Initialize(
                    itsTenantRepository,
                    itsGroupRepository,
                    itsRoleRepository,
                    itsConstraintRepository);
                result.Attach(Observers);
            }

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
            {
                result.Initialize(
                    itsTenantRepository,
                    itsGroupRepository,
                    itsRoleRepository,
                    itsConstraintRepository);
                result.Attach(Observers);
            }

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
                {
                    result.Initialize(
                        itsTenantRepository,
                        itsGroupRepository,
                        itsRoleRepository,
                        itsConstraintRepository);
                    result.Attach(Observers);
                }

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
                {
                    result.Initialize(
                        itsTenantRepository,
                        itsGroupRepository,
                        itsRoleRepository,
                        itsConstraintRepository);
                    result.Attach(Observers);
                }

            return results;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public IUser
        GetUniqueFor(IUserIdProvider provider)
        {
            if (provider != null)
                return GetUnique(provider.UserId);

            return null;
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
            {
                result.Initialize(
                    itsTenantRepository,
                    itsGroupRepository,
                    itsRoleRepository,
                    itsConstraintRepository);
                result.Attach(Observers);
            }

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
            {
                result.Initialize(
                    itsTenantRepository,
                    itsGroupRepository,
                    itsRoleRepository,
                    itsConstraintRepository);
                result.Attach(Observers);
            }

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
