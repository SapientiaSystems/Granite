//  ##########################################################################
//  # File Name: CreateExternalPlanRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

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
    class CreateExternalPlanRequest:
        ServiceRequest
    {
        [DataMember]
        public InitialPlanData PlanToCreate { get; set; }
    }
}

//  ##########################################################################
