//  ##########################################################################
//  # File Name: SessionRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.User;
using Strata.Persistence.NamedQuery;
using Strata.Persistence.Repository;
using Strata.Persistence.UnitOfWork;
using System;
using System.Collections.Generic;
using System.Linq.Expressions;
using SystemEx.Injection;

namespace Granite.Domain.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class SessionRepository:
        AbstractRepository<Guid,ISession>,
        ISessionRepository
    {
        private IUserRepository itsUserRepository;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        SessionRepository(
            IUnitOfWorkProvider provider,
            IUserRepository     userRepository):
            base(provider)
        {
            itsUserRepository = userRepository;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override ISession
        Insert(ISession session)
        {
            ISession result = base.Insert(session);

            result.Initialize(itsUserRepository);
            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override ISession
        Update(ISession session)
        {
            ISession result = base.Update(session);

            result.Initialize(itsUserRepository);
            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override ISession
        GetUnique(Guid sessionId)
        {
            ISession result = base.GetUnique(sessionId);

            result.Initialize(itsUserRepository);
            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override ISession
        GetUnique(Expression<Func<ISession,bool>> predicate)
        {
            ISession result = base.GetUnique(predicate);

            result.Initialize(itsUserRepository);
            return result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IList<ISession>
        GetAll()
        {
            IList<ISession> results = base.GetAll();

            foreach (ISession result in results)
                result.Initialize(itsUserRepository);

            return results;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override IList<ISession>
        GetAll(Expression<Func<ISession,bool>> predicate)
        {
            IList<ISession> results = base.GetAll(predicate);

            foreach (ISession result in results)
                result.Initialize(itsUserRepository);

            return results;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual ICollection<ISession>
        GetAllWithUserId(Guid userId)
        {
            INamedQuery<ISession> query =
                GetNamedQuery("Session.GetAllWithUserId");
            ICollection<ISession> results = null;

            query.SetInput("userId",userId);
            results = query.GetAll();

            foreach (ISession result in results)
                result.Initialize(itsUserRepository);

            return results;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual bool
        HasAnyWithUserId(Guid userId)
        {
            INamedQuery<ISession> query =
                GetNamedQuery("Session.HasAnylWithUserId");

            query.SetInput("userId",userId);
            return query.HasAny();
        }
    }
}

//  ##########################################################################
