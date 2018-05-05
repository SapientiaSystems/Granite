//  ##########################################################################
//  # File Name: CreateExternalUserRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Value;
using System;
using System.Runtime.Serialization;

namespace Granite.RequiredService.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [DataContract]
    public
    class CreateExternalUserRequest
    {
        [DataMember]
        public string        UserName { get; set; }
        [DataMember]
        public string        Password { get; set; }
        [DataMember]
        public PersonName    Name { get; set; }
        [DataMember]
        public string        JobTitle { get; set; }
        [DataMember]
        public PostalAddress Address { get; set; }
        [DataMember]
        public EmailAddress  Email { get; set; }
        [DataMember]
        public PhoneNumber   Phone { get; set; }
    }
}

//  ##########################################################################
