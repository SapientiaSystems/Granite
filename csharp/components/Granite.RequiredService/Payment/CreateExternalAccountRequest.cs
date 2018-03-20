//  ##########################################################################
//  # File Name: CreateExternalAccountRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.Runtime.Serialization;

namespace Granite.RequiredService.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class CreateExternalAccountRequest
    {
        [DataMember]
        public string Email { get; set;  }

        [DataMember]
        public string Description { get; set; }

        [DataMember]
        public string CreditCardNumber { get; set; }

        [DataMember]
        public int ExpirationYear { get; set; }

        [DataMember]
        public int ExpirationMonth { get; set; }

        [DataMember]
        public string SecurityCode { get; set; }
    }
}

//  ##########################################################################
