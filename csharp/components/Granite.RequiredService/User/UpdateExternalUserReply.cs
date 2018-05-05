//  ##########################################################################
//  # File Name: UpdateExternalUserReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using System.Runtime.Serialization;

namespace Granite.RequiredService.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [DataContract]
    public
    class UpdateExternalUserReply
    {
        [DataMember]
        public Guid   UserId { get; set; }
        [DataMember]
        public string ExternalUserId { get; set; }
        public bool IsSuccessful { get; set; }
        public string FailureMessage { get; set; }
        public string StackTrace { get; set; }
    }
}

//  ##########################################################################
