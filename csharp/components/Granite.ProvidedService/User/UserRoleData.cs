//  ##########################################################################
//  # File Name: UserRoleData.cs
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
    class UserRoleData
    {
        [DataMember]
        public string RoleName { get; set; }
        [DataMember]
        public string ConstraintId { get; set; }
    }
}

//  ##########################################################################
