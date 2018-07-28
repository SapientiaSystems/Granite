//  ##########################################################################
//  # File Name: UpdateExternalAccountReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

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
        public string ExternalAccountId { get; set; }
        public string ExternalPaymentMethodId { get; set; }
        public string Email { get; set; }
        public string Description { get; set; }
    }
}

//  ##########################################################################
