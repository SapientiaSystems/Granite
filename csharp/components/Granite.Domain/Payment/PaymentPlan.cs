//  ##########################################################################
//  # File Name: PaymentPlan.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################


using Granite.Common.Value;
using System;

namespace Granite.Domain.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class PaymentPlan
    {
        public virtual DateTime            InitialPaymentDate { get; set; }
        public virtual PaymentIntervalKind IntervalType { get; set; }
        public virtual int                 IntervalCount { get; set; }
        public virtual string              Currency { get; set; }
        public virtual decimal             Amount { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        PaymentPlan():
            this(DateTime.Now,PaymentIntervalKind.MONTH,1,0.0m) {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        PaymentPlan(PaymentIntervalKind intervalType,decimal amount):
            this(intervalType,1,amount) {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        PaymentPlan(
            PaymentIntervalKind intervalType,
            int                 intervalCount,
            decimal             amount):
            this(DateTime.Now,intervalType,intervalCount,amount) {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        PaymentPlan(
            DateTime            initialPaymentDate,
            PaymentIntervalKind intervalType,
            int                 intervalCount,
            decimal             amount):
            this(DateTime.Now,intervalType,intervalCount,"USD",amount) {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        PaymentPlan(
            DateTime            initialPaymentDate,
            PaymentIntervalKind intervalType,
            int                 intervalCount,
            string              currency,
            decimal             amount)
        {
            InitialPaymentDate = initialPaymentDate;
            IntervalType       = intervalType;
            IntervalCount      = intervalCount;
            Currency           = currency;
            Amount             = amount;
        }
    }
}

//  ##########################################################################
