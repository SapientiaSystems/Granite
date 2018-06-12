//  ##########################################################################
//  # File Name: RegisterReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Payment;
using Granite.ProvidedService.Profile;
using Granite.ProvidedService.Transfer;
using Granite.ProvidedService.User;
using System;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Registration
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class RegisterReply:
        GraniteReply
    {
        [DataMember]
        public RegistrationKind RegistrationType { get; set; }
        [DataMember]
        public UserData         NewUser { get; set; }
        [DataMember]
        public ProfileData      NewProfile { get; set; }
        [DataMember]
        public SubscriptionData NewSubscription { get; set; }
    }
}

//  ##########################################################################
