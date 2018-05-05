//  ##########################################################################
//  # File Name: CreateExternalUserReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

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
    class CreateExternalUserReply
    {
        [DataMember]
        public string ExternalUserId { get; set; }
        [DataMember]
        public bool   IsSuccessful { get; set; }
        [DataMember]
        public string FailureMessage { get; set; }
        [DataMember]
        public string StackTrace { get; set; }
    }
}

//  ##########################################################################
