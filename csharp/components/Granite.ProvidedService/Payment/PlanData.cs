//  ##########################################################################
//  # File Name: PlanData.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Value;
using System;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [DataContract]
    public
    class PlanData
    {
        [DataMember]
        public string              PlanName { get; set; }
        [DataMember]
        public PaymentIntervalKind IntervalType { get; set; }
        [DataMember]
        public int                 IntervalCount { get; set; }
        [DataMember]
        public string              Currency { get; set; }
        [DataMember]
        public decimal             Amount { get; set; }
    }
}

//  ##########################################################################
