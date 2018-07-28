//  ##########################################################################
//  # File Name: CreatePlanReply.cs
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
    class CreatePlanReply:
        GraniteReply
    {
        [DataMember]
        public PlanData NewPlan { get; set; }
    }
}

//  ##########################################################################
