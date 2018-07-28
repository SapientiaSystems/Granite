//  ##########################################################################
//  # File Name: CreateExternalAccountReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Transfer;
using System;
using System.Runtime.Serialization;

namespace Granite.RequiredService.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class CreateExternalAccountReply:
        ServiceReply
    {
        [DataMember]
        public string ExternalAccountId { get; set; }
        [DataMember]
        public string ExternalPaymentMethodId { get; set; }
        [DataMember]
        public string Email { get; set; }
        public Guid UserId { get; set; }
    }
}

//  ##########################################################################
