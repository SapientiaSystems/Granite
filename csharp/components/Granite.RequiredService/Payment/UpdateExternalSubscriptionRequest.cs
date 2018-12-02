//  ##########################################################################
//  # File Name: UpdateExternalSubscriptionRequest.cs
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
    class UpdateExternalSubscriptionRequest:
        ServiceRequest
    {
        [DataMember]
        public string ExternalSubscriptionId { get; set; }
        [DataMember]
        public string ExternalPlanId { get; set; }
    }
}

//  ##########################################################################
