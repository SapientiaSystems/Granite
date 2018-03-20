//  ##########################################################################
//  # File Name: CreateExternalUserRequest.cs
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
    class CreateExternalUserRequest
    {
        public string UserName { get; set; }
        public string Password { get; set; }
        public string EmailNickName { get; set; }
    }
}

//  ##########################################################################
