//  ##########################################################################
//  # File Name: GetOrLoginRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Transfer;
using System;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class GetOrLoginRequest:
        ServiceRequest
    {
        [DataMember]
        public string AccessToken { get; set; }
        [DataMember]
        public string UserName { get; set; }
        [DataMember]
        public string Password { get; set; }
    }
}

//  ##########################################################################
