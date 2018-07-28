//  ##########################################################################
//  # File Name: CreateAccountData.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Value;
using System;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class CreateAccountData
    {
        [DataMember]
        public Guid                 UserIdOfAccountToCreate { get; set; }
        [DataMember]
        public string               Email { get; set; }
        [DataMember]
        public string               Description { get; set; }
        [DataMember]
        public CreateCreditCardData CreditCardToCreate { get; set; }
    }
}

//  ##########################################################################
