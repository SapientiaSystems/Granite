//  ##########################################################################
//  # File Name: VerifyUserRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Shared;
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
    class VerifyUserRequest:
        GraniteRequest
    {
        [DataMember]
        public string UserNameToVerify { get; set; }
        [DataMember]
        public string VerificationCode { get; set; }
    }
}

//  ##########################################################################
