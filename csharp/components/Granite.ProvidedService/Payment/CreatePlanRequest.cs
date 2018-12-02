//  ##########################################################################
//  # File Name: CreatePlanRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Transfer;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class CreatePlanRequest:
        GraniteRequest
    {
        [DataMember]
        public InitialPlanData PlanToCreate { get; set; }
    }
}

//  ##########################################################################
