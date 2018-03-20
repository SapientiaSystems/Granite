//  ##########################################################################
//  # File Name: AuthenticateRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.Runtime.Serialization;

namespace Granite.RequiredService.Authentication
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [DataContract]
    public
    class AuthenticateRequest
    {
        [DataMember]
        public string Username { get; set; }
        [DataMember]
        public string Password { get; set; }
    }
}

//  ##########################################################################
