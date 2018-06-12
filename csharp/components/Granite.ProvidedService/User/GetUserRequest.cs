//  ##########################################################################
//  # File Name: GetUserRequest.cs
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
    class GetUserRequest:
        GraniteRequest
    {
        [DataMember]
        public bool   GetCurrentUser { get; set; }
        [DataMember]
        public string UserNameOfUserToGet { get; set; }
        [DataMember]
        public Guid   UserIdOfUserToGet { get; set; }

        public
        GetUserRequest()
        {
            GetCurrentUser = false;
            UserNameOfUserToGet = null;
            UserIdOfUserToGet = Guid.Empty;
        }
    }
}

//  ##########################################################################
