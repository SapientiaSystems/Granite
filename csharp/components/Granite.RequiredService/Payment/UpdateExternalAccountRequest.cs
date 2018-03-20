//  ##########################################################################
//  # File Name: UpdateExternalAccountRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

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
    class UpdateExternalAccountRequest
    {
        [DataMember]
        public string        AccountId { get; set; }
        [DataMember]
        public string        PaymentMethodId { get; set; }
        [DataMember]
        public string        Email { get; set; }
        [DataMember]
        public string        Description { get; set; }
        [DataMember]
        public string        CreditCardNumber { get; set; }
        [DataMember]
        public Nullable<int> ExpirationYear { get; set; }
        [DataMember]
        public Nullable<int> ExpirationMonth { get; set; }
        [DataMember]
        public string        SecurityCode { get; set; }

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
        HasAccountId()
        {
            return !string.IsNullOrEmpty(AccountId);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public bool
        HasPaymentMethodId()
        {
            return !string.IsNullOrEmpty(PaymentMethodId);
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

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public bool
        HasCreditCardNumber()
        {
            return !string.IsNullOrEmpty(CreditCardNumber);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public bool
        HasExpirationYear()
        {
            return ExpirationYear.HasValue;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public bool
        HasExpirationMonth()
        {
            return ExpirationMonth.HasValue;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public bool
        HasSecurityCode()
        {
            return !string.IsNullOrEmpty(SecurityCode);
        }
    }
}

//  ##########################################################################
