//  ##########################################################################
//  # File Name: DeleteUserRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Shared;
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
        public Guid   UserIdToDelete { get; set; }
        [DataMember]
        public string UserNameToDelete { get; set; }
    }
}

//  ##########################################################################
