//  ##########################################################################
//  # File Name: AuthenticateReply.cs
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
    class AuthenticateReply
    {
        [DataMember]
        public string Username { get; set; }
        [DataMember]
        public string Password { get; set; }
        [DataMember]
        public string AccessToken { get; set; }
        [DataMember]
        public string AccessTokenType { get; set; }
        [DataMember]
        public string RefreshToken { get; set; }
    }
}

//  ##########################################################################
