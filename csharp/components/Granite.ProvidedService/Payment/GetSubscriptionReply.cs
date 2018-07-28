//  ##########################################################################
//  # File Name: GetSubscriptionReply.cs
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
    class GetSubscriptionReply:
        GraniteReply
    {
        [DataMember]
        public SubscriptionData RequestedSubscription { get; set; }
    }
}

//  ##########################################################################
