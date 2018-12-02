//  ##########################################################################
//  # File Name: CreateExternalSubscriptionRequest.cs
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
    class CreateExternalSubscriptionRequest:
        ServiceRequest
    {
        [DataMember]
        public string                 ExternalAccountId { get; set; }
        [DataMember]
        public InitialSubscriptionData SubscriptionToCreate { get; set; }
    }
}

//  ##########################################################################
