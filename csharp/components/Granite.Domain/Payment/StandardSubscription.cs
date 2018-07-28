//  ##########################################################################
//  # File Name: StandardSubscription.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Domain.Shared;
using System;

namespace Granite.Domain.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class StandardSubscription:
        AbstractEntity<
            Guid,
            ISubscription,
            ISubscriptionDomainEvent,
            ISubscriptionDomainEventObserver>,
        ISubscription
    {
        public virtual Guid
        SubscriptionId
        {
            get { return PrimaryId; }
            set { PrimaryId = value; }
        }

        public virtual Guid        AccountId { get; set; }
        public virtual Guid        PlanId { get; set; }
        public virtual Guid        UserId { get; set; }

        public virtual string      ExternalSubscriptionId { get; set; }
        public virtual string      ExternalPlanId { get; set; }

        public virtual DateTime    InitialPaymentDate { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        StandardSubscription()
        {
            ExternalSubscriptionId = null;
            ExternalPlanId = null;
            InitialPaymentDate = DateTime.Now;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected override ISubscription
        GetSelf()
        {
            return this;
        }
    }
}

//  ##########################################################################
