//  ##########################################################################
//  # File Name: UserGroupData.cs
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
    class UserGroupData
    {
        [DataMember]
        public Guid           TenantId { get; set; }
        [DataMember]
        public string         GroupName { get; set; }
        [DataMember]
        public UserRoleData[] Roles { get; set; }
    }
}

//  ##########################################################################
