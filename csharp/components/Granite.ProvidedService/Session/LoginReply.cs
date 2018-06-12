//  ##########################################################################
//  # File Name: LoginReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Transfer;
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
        ServiceReply
    {
        [DataMember]
        public SessionData NewSession { get; set; }
    }
}

//  ##########################################################################
