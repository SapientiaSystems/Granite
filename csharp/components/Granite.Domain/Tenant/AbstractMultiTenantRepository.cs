//  ##########################################################################
//  # File Name: AbstractMultiTenantRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.NamedQuery;
using Strata.Domain.Repository;
using Strata.Domain.UnitOfWork;
using System;
using System.Collections.Generic;
using System.Linq.Expressions;

namespace Granite.Domain.Tenant
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public abstract
    class AbstractMultiTenantRepository<K,E>:
        AbstractRepository<MultiTenantId<K>,E>,
        IMultiTenantRepository<K,E>
        where E : class,IMultiTenantEntity<K>
    {
        public Guid           ActiveTenantId { get; protected set; }
        protected Stack<Guid> ActiveTenantIdScope { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected
        AbstractMultiTenantRepository(
            IUnitOfWorkProvider provider,
            ITenantRepository   tenants):
            base(provider)
        {
            ActiveTenantId = Guid.Empty;
            ActiveTenantIdScope = new Stack<Guid>();
            tenants.Observers.Add(this);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override E
        Insert(E entity)
        {
            CheckTenantActive();
            entity.SetTenantId(ActiveTenantId);
            return base.Insert(entity);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override E
        Update(E entity)
        {
            CheckTenantId(entity);
            return base.Update(entity);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override void
        Remove(E entity)
        {
            CheckTenantId(entity);
            base.Remove(entity);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override E 
        GetUnique(MultiTenantId<K> key)
        {
            CheckTenantActive();
            return base.GetUnique(key);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override E
        GetUnique(Expression<Func<E,bool>> predicate)
        {
            Expression<Func<E,bool>> whereTenantId =
                (E e) => 
                    e.MultiTenantPrimaryId.TenantId.Equals(ActiveTenantId);

            CheckTenantActive();

            return 
                base.GetUnique(
                    Expression.Lambda<Func<E,bool>>(
                        Expression.AndAlso(predicate,whereTenantId),
                        predicate.Parameters));
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override IList<E>
        GetAll(Expression<Func<E,bool>> predicate)
        {
            Expression<Func<E,bool>> whereTenantId =
                (E e) =>
                    e.MultiTenantPrimaryId.TenantId.Equals(ActiveTenantId);

            CheckTenantActive();

            return
                base.GetAll(
                    Expression.Lambda<Func<E,bool>>(
                        Expression.AndAlso(predicate,whereTenantId),
                        predicate.Parameters));
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override IList<E>
        GetAll()
        {
            CheckTenantActive();

            return 
                base.GetAll(
                    e => e.MultiTenantPrimaryId.TenantId.Equals(
                        ActiveTenantId));
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override INamedQuery<E>
        GetNamedQuery(string queryName)
        {
            CheckTenantActive();
            return base.GetNamedQuery(queryName);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override bool
        HasAny(Expression<Func<E,bool>> predicate)
        {
            Expression<Func<E,bool>> whereTenantId =
                (E e) =>
                    e.MultiTenantPrimaryId.TenantId.Equals(ActiveTenantId);

            CheckTenantActive();

            return
                base.HasAny(
                    Expression.Lambda<Func<E,bool>>(
                        Expression.AndAlso(predicate,whereTenantId),
                        predicate.Parameters));
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual E
        GetUnique(K key)
        {
            CheckTenantActive();
            return GetUnique(new MultiTenantId<K>(ActiveTenantId,key));
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual bool
        HasUnique(K key)
        {
            CheckTenantActive();
            return HasUnique(new MultiTenantId<K>(ActiveTenantId,key));
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void 
        OnEvent(ITenantDomainEvent evnt)
        {
            if (evnt is TenantActivatedDomainEvent)
            {
                ActiveTenantIdScope.Push(ActiveTenantId);
                ActiveTenantId = evnt.Source.TenantId;
            }
            else if (evnt is TenantDeactivatedDomainEvent)
            {
                ActiveTenantId =
                    ActiveTenantIdScope.Count > 0
                        ? ActiveTenantIdScope.Pop()
                        : Guid.Empty;
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void 
        OnException(Exception exception)
        {
            throw exception;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected void
        CheckTenantActive()
        {
            if (ActiveTenantId.Equals(Guid.Empty))
                throw
                    new InvalidOperationException(
                        "There is no active tenant");
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected void
        CheckTenantId(E entity)
        {
            CheckTenantActive();

            if (entity.MultiTenantPrimaryId == null)
                throw
                    new NullReferenceException(
                        "Entity.MultiTenantPrimaryId is null");

            if (!entity.MultiTenantPrimaryId.TenantId.Equals(ActiveTenantId))
                throw
                    new ArgumentException(
                        string.Format(
                            "Entity tenantId {0} " +
                            "does not match ActiveTenantId {1}",
                            entity.MultiTenantPrimaryId.TenantId,
                            ActiveTenantId));
        }
    }
}

//  ##########################################################################
