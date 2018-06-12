//  ##########################################################################
//  # File Name: SubscriptionData.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [DataContract]
    public
    class SubscriptionData
    {
        [DataMember]
        public Guid SubscriptionId { get; set; }
    }
}

//  ##########################################################################
