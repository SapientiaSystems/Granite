//  ##########################################################################
//  # File Name: UpdateSubscriptionRequest.cs
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
    class UpdateSubscriptionRequest:
        GraniteRequest
    {
        [DataMember]
        public SubscriptionData        CurrentSubscription { get; set; }
        [DataMember]
        public InitialSubscriptionData NewSubscription { get; set; }
    }
}

//  ##########################################################################
