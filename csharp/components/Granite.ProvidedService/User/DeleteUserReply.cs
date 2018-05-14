//  ##########################################################################
//  # File Name: DeleteUserReply.cs
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
    class DeleteUserReply:
        GraniteReply
    {
        [DataMember]
        public Guid UserIdDeleted { get; set; }
    }
}

//  ##########################################################################
