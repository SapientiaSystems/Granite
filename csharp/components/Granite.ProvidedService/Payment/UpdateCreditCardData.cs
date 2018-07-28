//  ##########################################################################
//  # File Name: UpdateCreditCardData.cs
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
    class UpdateCreditCardData
    {
        [DataMember]
        public string        CardHolderName { get; set; }
        [DataMember]
        public PostalAddress BillingAddress { get; set; }
        [DataMember]
        public int           ExpirationMonth { get; set; }
        [DataMember]
        public int           ExpirationYear { get; set; }
    }
}

//  ##########################################################################
