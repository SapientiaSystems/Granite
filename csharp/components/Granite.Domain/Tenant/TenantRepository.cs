//  ##########################################################################
//  # File Name: TenantRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.NamedQuery;
using Strata.Domain.Repository;
using Strata.Domain.UnitOfWork;
using System;
using System.Collections.Generic;
using System.Linq.Expressions;
using SystemEx.Injection;

namespace Granite.Domain.Tenant
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class TenantRepository:
        AbstractRepository<Guid,ITenant>,
        ITenantRepository
    {
        public ISet<ITenantDomainEventObserver> 
        Observers { get; protected set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        TenantRepository(IUnitOfWorkProvider provider):
            base(provider)
        {
            Observers = new HashSet<ITenantDomainEventObserver>();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override ITenant 
        Insert(ITenant entity)
        {
            ITenant result = base.Insert(entity);

            if (result != null)
                result.Attach(Observers);

            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override ITenant 
        Update(ITenant entity)
        {
            ITenant result = base.Update(entity);

            if (result != null)
                result.Attach(Observers);

            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override ITenant 
        GetUnique(Guid key)
        {
            ITenant result = base.GetUnique(key);

            if (result != null)
                result.Attach(Observers);

            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override ITenant 
        GetUnique(Expression<Func<ITenant,bool>> predicate)
        {
            ITenant result = base.GetUnique(predicate);

            if (result != null)
                result.Attach(Observers);

            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IList<ITenant> 
        GetAll(Expression<Func<ITenant,bool>> predicate)
        {
            IList<ITenant> results = base.GetAll(predicate);

            foreach (ITenant result in results)
                if (result != null)
                    result.Attach(Observers);

            return results;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IList<ITenant> 
        GetAll()
        {
            IList<ITenant> results = base.GetAll();

            foreach (ITenant result in results)
                if (result != null)
                    result.Attach(Observers);

            return results;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual ITenant
        GetUniqueFor(ITenantIdProvider provider)
        {
            return GetUnique(provider.TenantId);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual ITenant
        GetUniqueWithTenantName(string tenantName)
        {
            INamedQuery<ITenant> query =
                GetNamedQuery("Tenant.GetUniqueWithTenantName");
            ITenant result = null;

            query.SetInput("tenantName",tenantName);
            result = query.GetUnique();

            if (result != null)
                result.Attach(Observers);

            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual bool
        HasUniqueFor(ITenantIdProvider provider)
        {
            return HasUnique(provider.TenantId);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual bool
        HasUniqueWithTenantName(string tenantName)
        {
            INamedQuery<ITenant> query =
                GetNamedQuery("Tenant.HasUniqueWithTenantName");

            query.SetInput("tenantName",tenantName);
            return query.HasUnique();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public static Guid
        GetKey(ITenant tenant)
        {
            return tenant.PrimaryId;
        }
    }
}

//  ##########################################################################
