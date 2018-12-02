//  ##########################################################################
//  # File Name: ProfileData.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Value;
using System;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class ProfileData
    {
        [DataMember]
        public Guid          ProfileId { get; set; }
        [DataMember]
        public Guid          UserId { get; set; }
        [DataMember]
        public string        UserName { get; set; }
        [DataMember]
        public PersonName    Name { get; set; }
        [DataMember]
        public string        JobTitle { get; set; }
        [DataMember]
        public EmailAddress  Email { get; set; }
        [DataMember]
        public PostalAddress Address { get; set; }
        [DataMember]
        public PhoneNumber   Phone { get; set; }
    }
}

//  ##########################################################################
