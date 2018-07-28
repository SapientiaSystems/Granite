//  ##########################################################################
//  # File Name: PlanRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Domain.NamedQuery;
using Strata.Domain.Repository;
using Strata.Domain.UnitOfWork;
using System;
using SystemEx.Injection;

namespace Granite.Domain.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class PlanRepository:
        AbstractRepository<Guid,IPlan>,
        IPlanRepository
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        PlanRepository(IUnitOfWorkProvider provider):
            base(provider) {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public IPlan 
        GetUniqueWithPlanName(string planName)
        {
            INamedQuery<IPlan> query =
                GetNamedQuery("Plan.GetUniqueWithPlanName");

            query.SetInput("planName",planName);
            return query.GetUnique();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public bool 
        HasUniqueWithPlanName(string planName)
        {
            INamedQuery<IPlan> query =
                GetNamedQuery("Plan.HasUniqueWithPlanName");

            query.SetInput("planName",planName);
            return query.HasUnique();
        }
    }
}

//  ##########################################################################
