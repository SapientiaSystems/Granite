//  ##########################################################################
//  # File Name: LoginReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Shared;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [DataContract]
    public
    class LoginReply:
        GraniteReply
    {
        [DataMember]
        public SessionData NewSession { get; set; }
    }
}

//  ##########################################################################
