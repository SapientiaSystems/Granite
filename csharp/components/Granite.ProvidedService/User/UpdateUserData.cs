//  ##########################################################################
//  # File Name: UpdateUserData.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Value;
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
    class UpdateUserData
    {
        [DataMember]
        public UserUpdateKind Operation { get; set; }
        [DataMember]
        public bool           CurrentUser { get; set; }
        [DataMember]
        public Guid           UserIdOfUserToUpdate { get; set; }
        [DataMember]
        public string         UserNameOfUserToUpdate { get; set; }
        [DataMember]
        public string         CurrentPassword { get; set; }
        [DataMember]
        public string         NewPassword { get; set; }
        [DataMember]
        public EmailAddress   NewEmail { get; set; }
    }
}

//  ##########################################################################
