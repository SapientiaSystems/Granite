//  ##########################################################################
//  # File Name: DeleteExternalPlanRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

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
    class DeleteExternalPlanRequest:
        ServiceRequest
    {
        public string ExternalPlanId { get; set; }
    }
}

//  ##########################################################################
