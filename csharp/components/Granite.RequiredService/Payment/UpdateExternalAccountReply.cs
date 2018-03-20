//  ##########################################################################
//  # File Name: UpdateExternalAccountReply.cs
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
    class UpdateExternalAccountReply
    {
        public string AccountId { get; set; }
        public string Email { get; set; }
        public string Description { get; set; }
        public string PaymentMethodId { get; set; }
    }
}

//  ##########################################################################
