//  ##########################################################################
//  # File Name: GetAccountRequest.cs
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
    class GetAccountRequest:
        GraniteRequest
    {
        [DataMember]
        public bool   GetAccountOfCurrentUser { get; set; }
        [DataMember]
        public Guid   AccountIdOfAccountToGet { get; set; }
        [DataMember]
        public Guid   UserIdOfAccountToGet { get; set; }
        [DataMember]
        public string UserNameOfAccountToGet { get; set; }


    }
}

//  ##########################################################################
