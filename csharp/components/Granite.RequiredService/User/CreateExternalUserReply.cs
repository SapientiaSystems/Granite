//  ##########################################################################
//  # File Name: CreateExternalUserReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using System.Runtime.Serialization;
using Strata.Foundation.Value;

namespace Granite.RequiredService.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [DataContract]
    public
    class CreateExternalUserReply
    {
        [DataMember]
        public string       ExternalUserId { get; set; }
        [DataMember]
        public string       UserName { get; set; }
        [DataMember]
        public EmailAddress Email { get; set; }
        [DataMember]
        public string       JobTitle { get; set; }
        [DataMember]
        public bool         IsSuccessful { get; set; }
        [DataMember]
        public string       FailureMessage { get; set; }
        [DataMember]
        public string       StackTrace { get; set; }
        [DataMember]
        public Exception    Cause { get; set; }
    }
}

//  ##########################################################################
