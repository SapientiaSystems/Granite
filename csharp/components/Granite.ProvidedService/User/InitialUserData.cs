//  ##########################################################################
//  # File Name: InitialUserData.cs
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
    class InitialUserData
    {
        [DataMember]
        public string       UserName { get; set; }
        [DataMember]
        public string       Password { get; set; }
        [DataMember]
        public EmailAddress Email { get; set; }
        [DataMember]
        public bool         UseExistingTenant { get; set; }
        [DataMember]
        public Guid         TenantId { get; set; }
        [DataMember]
        public bool         HasSubscriptionType { get; set; }
        [DataMember]
        public string       SubscriptionType { get; set; }
    }
}

//  ##########################################################################
