//  ##########################################################################
//  # File Name: GetSubscriptionRequest.cs
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
    class GetSubscriptionRequest:
        GraniteRequest
    {
        [DataMember]
        public bool   GetSubscriptionOfCurrentUser { get; set; }
        [DataMember]
        public string UserNameOfSubscriptionToGet { get; set; }
        [DataMember]
        public Guid   UserIdOfSubscriptionToGet { get; set; }
        [DataMember]
        public Guid   AccountIdOfSubscriptionToGet { get; set; }
        [DataMember]
        public Guid   SubscriptionIdOfSubscriptionToGet { get; set; }


    }
}

//  ##########################################################################
