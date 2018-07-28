//  ##########################################################################
//  # File Name: StandardPlan.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Value;
using Strata.Domain.Shared;
using System;

namespace Granite.Domain.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class StandardPlan:
        AbstractEntity<Guid>,
        IPlan
    {
        public virtual Guid
        PlanId
        {
            get { return PrimaryId; }
            set { PrimaryId = value; }
        }

        public virtual string              PlanName { get; set; }
        public virtual string              ExternalPlanId { get; set; }

        public virtual PaymentIntervalKind IntervalType { get; set; }
        public virtual int                 IntervalCount { get; set; }

        public virtual string              Currency { get; set; }
        public virtual decimal             Amount { get; set; }
    }
}

//  ##########################################################################
