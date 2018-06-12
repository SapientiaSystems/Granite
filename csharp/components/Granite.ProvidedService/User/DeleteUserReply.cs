//  ##########################################################################
//  # File Name: DeleteUserReply.cs
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
    class DeleteUserReply:
        GraniteReply
    {
        [DataMember]
        public Guid DeletedUserId { get; set; }
        [DataMember]
        public bool HasAlsoDeletedTenant { get; set; }
        [DataMember]
        public Guid DeletedTenantId { get; set; }
    }
}

//  ##########################################################################
