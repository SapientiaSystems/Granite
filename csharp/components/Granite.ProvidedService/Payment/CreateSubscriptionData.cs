//  ##########################################################################
//  # File Name: CreateSubscriptionData.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

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
    class CreateSubscriptionData
    {
        [DataMember]
        public string            SubscriptionType { get; set; }
        [DataMember]
        public Guid              UserId { get; set; }
        [DataMember]
        public Guid              AccountId { get; set; }
    }
}

//  ##########################################################################
