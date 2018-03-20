//  ##########################################################################
//  # File Name: CreateExternalAccountReply.cs
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
    class CreateExternalAccountReply
    {
        [DataMember]
        public string AccountId { get; set; }

        [DataMember]
        public string Email { get; set; }
        public string PaymentMethodId { get; set; }
    }
}

//  ##########################################################################
