//  ##########################################################################
//  # File Name: CreateExternalPlanReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Value;
using Granite.ProvidedService.Payment;
using Strata.Foundation.Transfer;
using System.Runtime.Serialization;

namespace Granite.RequiredService.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class CreateExternalPlanReply:
        ServiceReply
    {
        public string PlanName { get; set; }
        public string ExternalPlanId { get; set; }
        public PaymentIntervalKind IntervalType { get; set; }
        public int IntervalCount { get; set; }
        public string Currency { get; set; }
        public decimal Amount { get; set; }
        public PlanData NewPlan { get; set; }
    }
}

//  ##########################################################################
