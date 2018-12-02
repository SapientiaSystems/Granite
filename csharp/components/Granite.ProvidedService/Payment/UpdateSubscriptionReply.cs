//  ##########################################################################
//  # File Name: UpdateSubscriptionReply.cs
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
    class UpdateSubscriptionReply:
        GraniteReply
    {
        [DataMember]
        public SubscriptionData UpdatedSubscription { get; set; }
    }
}

//  ##########################################################################
