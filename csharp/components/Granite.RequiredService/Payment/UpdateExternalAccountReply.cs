//  ##########################################################################
//  # File Name: UpdateExternalAccountReply.cs
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
    class UpdateExternalAccountReply:
        ServiceReply
    {
        [DataMember]
        public string         ExternalAccountId { get; set; }
        [DataMember]
        public string         ExternalPaymentMethodId { get; set; }
        [DataMember]
        public string         Email { get; set; }
        [DataMember]
        public string         Description { get; set; }
        [DataMember]
        public CreditCardData CreditCard { get; set; }
    }
}

//  ##########################################################################
