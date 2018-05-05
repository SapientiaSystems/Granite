//  ##########################################################################
//  # File Name: DeleteExternalUserRequest.cs
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
    class DeleteExternalUserRequest
    {
        public Guid   UserId { get; set; }
        public string ExternalUserId { get; set; }
    }
}

//  ##########################################################################
