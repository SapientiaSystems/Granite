//  ##########################################################################
//  # File Name: GroupRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Role;
using Granite.Domain.Shared;
using Granite.Domain.Tenant;
using Strata.Domain.NamedQuery;
using Strata.Domain.UnitOfWork;
using System;
using System.Collections.Generic;
using System.Linq.Expressions;
using SystemEx.Injection;

namespace Granite.Domain.Group
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary> 
    /// </summary>
    ///  
    public
    class GroupRepository:
        AbstractMultiTenantRepository<string,IGroup>,
        IGroupRepository
    {
        private IRoleRepository itsRoleRepository;

        //////////////////////////////////////////////////////////////////////
        /// <summary> 
        /// </summary>
        ///  
        [Inject]
        public
        GroupRepository(
            IUnitOfWorkProvider provider,
            ITenantRepository   tenants,
            IRoleRepository     roles):
            base(provider,tenants)
        {
            itsRoleRepository = roles;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IGroup
        Insert(IGroup group)
        {
            IGroup result = base.Insert(group);

            if (result != null)
                result.Initialize(itsRoleRepository);

            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IGroup
        Update(IGroup group)
        {
            IGroup result = base.Update(group);

            if (result != null)
                result.Initialize(itsRoleRepository);

            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IGroup
        GetUnique(string groupName)
        {
            IGroup result = base.GetUnique(groupName);

            if (result != null)
                result.Initialize(itsRoleRepository);

            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IGroup
        GetUnique(Expression<Func<IGroup,bool>> predicate)
        {
            IGroup result = base.GetUnique(predicate);

            if (result != null)
                result.Initialize(itsRoleRepository);

            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IList<IGroup>
        GetAll()
        {
            IList<IGroup> results = base.GetAll();

            foreach (IGroup result in results)
                if (result != null)
                    result.Initialize(itsRoleRepository);

            return results;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IList<IGroup>
        GetAll(Expression<Func<IGroup,bool>> predicate)
        {
            IList<IGroup> results = base.GetAll(predicate);

            foreach (IGroup result in results)
                if (result != null)
                    result.Initialize(itsRoleRepository);

            return results;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual ICollection<IGroup>
        GetGroupsFor(IUserIdProvider user)
        {
            INamedQuery<IGroup> query = GetNamedQuery("Group.GetGroupsFor");
            ICollection<IGroup> results = null;

            query.SetInput("userId",user.UserId);
            results = query.GetAll();

            foreach (IGroup result in results)
                if (result != null)
                    result.Initialize(itsRoleRepository);

            return results;
        }

    }
}

//  ##########################################################################
