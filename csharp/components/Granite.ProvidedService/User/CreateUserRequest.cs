//  ##########################################################################
//  # File Name: CreateUserRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Shared;
using Strata.Foundation.Value;
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
    class CreateUserRequest:
        GraniteRequest
    {
        [DataMember]
        public string        UserName { get; set; }
        [DataMember]
        public string        Password { get; set; }
        [DataMember]
        public EmailAddress  Email { get; set; }
        [DataMember]
        public PersonName    Name { get; set; }
        [DataMember]
        public string        JobTitle { get; set; }
        [DataMember]
        public string[]      AuthorizedRoles { get; set; }
        [DataMember]
        public PostalAddress Address { get; set; }
        [DataMember]
        public PhoneNumber   Phone { get; set; }
    }
}

//  ##########################################################################
