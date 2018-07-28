//  ##########################################################################
//  # File Name: CreateSubscriptionReply.cs
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
    class CreateSubscriptionReply:
        GraniteReply
    {
        [DataMember]
        public SubscriptionData NewSubscription { get; set; }
    }
}

//  ##########################################################################
