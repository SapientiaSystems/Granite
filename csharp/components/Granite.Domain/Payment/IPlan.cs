//  ##########################################################################
//  # File Name: IPlan.cs
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
    interface IPlan:
        IEntity<Guid>
    {
        Guid                PlanId { get; set; }
        string              PlanName { get; set; }
        string              ExternalPlanId { get; set; }

        PaymentIntervalKind IntervalType { get; set; }
        int                 IntervalCount { get; set; }

        string              Currency { get; set; }
        decimal             Amount { get; set; }
    }
}

//  ##########################################################################
