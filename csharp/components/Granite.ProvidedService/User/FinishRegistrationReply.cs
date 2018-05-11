//  ##########################################################################
//  # File Name: FinishRegistrationReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Shared;
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
    class FinishRegistrationReply:
        GraniteReply
    {
        [DataMember]
        public Guid   UserIdVerified { get; set; }
        [DataMember]
        public string SuccessMessage { get; set; }
    }
}

//  ##########################################################################
