//  ##########################################################################
//  # File Name: CreateUserReply.cs
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
    class CreateUserReply:
        GraniteReply
    {
        [DataMember]
        public string       UserName { get; set; }
        [DataMember]
        public EmailAddress Email { get; set; }
        [DataMember]
        public string       JobTitle { get; set; }
        [DataMember]
        public string       SuccessMessage { get; set; }
    }
}

//  ##########################################################################
