//  ##########################################################################
//  # File Name: FinishRegistrationRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Service.Core;
using System;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [DataContract]
    public
    class FinishRegistrationRequest:
        ServiceRequest
    {
        [DataMember]
        public Guid   RegistrationId { get; set; }
        [DataMember]
        public string RegistrationCode { get; set; }
    }
}

//  ##########################################################################
