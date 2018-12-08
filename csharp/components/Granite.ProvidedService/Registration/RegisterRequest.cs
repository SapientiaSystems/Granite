//  ##########################################################################
//  # File Name: RegisterRequest.cs
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
    class RegisterRequest:
        GraniteRequest
    {
        [DataMember]
        public RegistrationKind       RegistrationType { get; set; }
        [DataMember]
        public InitialUserData         UserToCreate { get; set; }
        [DataMember]
        public CreateOrUpdateProfileData      
                                      ProfileToCreate { get; set; }
        [DataMember]
        public InitialAccountData      AccountToCreate { get; set; }
        [DataMember]
        public InitialSubscriptionData SubscriptionToCreate { get; set; }
    }
}

//  ##########################################################################
