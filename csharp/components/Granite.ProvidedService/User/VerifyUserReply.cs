//  ##########################################################################
//  # File Name: VerifyUserReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Transfer;
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
    class VerifyUserReply:
        GraniteReply
    {
        [DataMember]
        public Guid   UserIdVerified { get; set; }
    }
}

//  ##########################################################################
