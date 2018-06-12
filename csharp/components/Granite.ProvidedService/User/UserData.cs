//  ##########################################################################
//  # File Name: UserData.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class UserData
    {
        [DataMember]
        public Guid            TenantId { get; set; }
        [DataMember]
        public Guid            UserId { get; set; }
        [DataMember]
        public string          UserName { get; set; }
        [DataMember]
        public string          Email { get; set; }
        [DataMember]
        public UserRoleData[]  Roles { get; set; }
        [DataMember]
        public UserGroupData[] Groups { get; set; }
    }
}

//  ##########################################################################
