//  ##########################################################################
//  # File Name: GetExternalAccountRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

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
    class GetExternalAccountRequest:
        ServiceRequest
    {
        [DataMember]
        public string ExternalAccountId { get; set; }
        [DataMember]
        public string ExternalPaymentMethodId { get; set; }
    }
}

//  ##########################################################################
