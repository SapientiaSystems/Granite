//  ##########################################################################
//  # File Name: DeletePlanRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Transfer;
using System;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class DeletePlanRequest:
        GraniteRequest
    {
        public Guid PlanIdOfPlanToDelete { get; set; }
    }
}

//  ##########################################################################
