//  ##########################################################################
//  # File Name: DeleteExternalUserReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;

namespace Granite.RequiredService.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class DeleteExternalUserReply
    {
        public Guid   UserId { get; set; }
        public string ExternalUserId { get; set; }
        public bool   IsSuccessful { get; set; }
        public string FailureMessage { get; set; }
        public string StackTrace { get; set; }
    }
}

//  ##########################################################################
