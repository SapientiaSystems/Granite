//  ##########################################################################
//  # File Name: UpdateExternalAccountRequest.cs
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
    class UpdateExternalAccountRequest:
        ServiceRequest
    {
        [DataMember]
        public string               ExternalAccountId { get; set; }
        [DataMember]
        public string               ExternalPaymentMethodId { get; set; }
        [DataMember]
        public string               Email { get; set; }
        [DataMember]
        public string               Description { get; set; }
        [DataMember]
        public bool                 ReplaceCreditCard { get; set; }
        [DataMember]
        public InitialCreditCardData CreditCardToCreate { get; set; }
        [DataMember]
        public UpdateCreditCardData CreditCardToUpdate { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        UpdateExternalAccountRequest()
        {
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public bool
        HasExternalAccountId()
        {
            return !string.IsNullOrEmpty(ExternalAccountId);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public bool
        HasExternalPaymentMethodId()
        {
            return !string.IsNullOrEmpty(ExternalPaymentMethodId);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public bool
        HasEmail()
        {
            return !string.IsNullOrEmpty(Email);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public bool
        HasDescription()
        {
            return !string.IsNullOrEmpty(Description);
        }
    }
}

//  ##########################################################################
