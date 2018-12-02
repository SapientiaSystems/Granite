//  ##########################################################################
//  # File Name: AccountData.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Value;
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
    class AccountData
    {
        [DataMember]
        public Guid           UserId { get; set; }
        [DataMember]
        public Guid           AccountId { get; set; }
        [DataMember]
        public string         PaymentMethod { get; set; }
        [DataMember]
        public CreditCardData CreditCard { get; set; }
    }
}

//  ##########################################################################
