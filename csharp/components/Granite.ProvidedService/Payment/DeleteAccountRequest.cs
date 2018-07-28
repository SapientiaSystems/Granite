//  ##########################################################################
//  # File Name: DeleteAccountRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Transfer;
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
    class DeleteAccountRequest:
        GraniteRequest
    {
        [DataMember]
        public Guid UserIdOfAccountToDelete { get; set; }
        [DataMember]
        public Guid AccountIdOfAccountToDelete { get; set; }
    }
}

//  ##########################################################################
