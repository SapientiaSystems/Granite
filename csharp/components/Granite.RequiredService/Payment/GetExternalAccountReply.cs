//  ##########################################################################
//  # File Name: GetExternalAccountReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Payment;
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
    class GetExternalAccountReply:
        ServiceReply
    {
        [DataMember]
        public string         ExternalAccountId { get; set; }
        [DataMember]
        public string         ExternalPaymentMethodId { get; set; }
        [DataMember]
        public string         Email { get; set; }
        [DataMember]
        public Guid           UserId { get; set; }
        [DataMember]
        public CreditCardData CreditCard { get; set; }
    }
}

//  ##########################################################################
