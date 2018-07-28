//  ##########################################################################
//  # File Name: DeleteExternalAccountRequest.cs
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
    class DeleteExternalAccountRequest:
        ServiceRequest
    {
        [DataMember]
        public string ExternalAccountId { get; set; }
    }
}

//  ##########################################################################
