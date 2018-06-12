//  ##########################################################################
//  # File Name: DeleteUserRequest.cs
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
    class DeleteUserRequest:
        GraniteRequest
    {
        [DataMember]
        public Guid   UserIdOfUserToDelete { get; set; }
        [DataMember]
        public string UserNameOfUserToDelete { get; set; }
    }
}

//  ##########################################################################
