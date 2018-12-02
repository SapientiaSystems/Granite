//  ##########################################################################
//  # File Name: UpdateAccountData.cs
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
    class UpdateAccountData
    {
        [DataMember]
        public Guid                 UserIdOfAccountToUpdate { get; set; }
        [DataMember]
        public Guid                 AccountIdOfAccountToUpdate { get; set; }
        [DataMember]
        public string               Email { get; set; }
        [DataMember]
        public string               Description { get; set; }
        [DataMember]
        public bool                 ReplaceCreditCard { get; set; }
        [DataMember]
        public InitialCreditCardData CreditCardToCreate { get; set; }
        [DataMember]
        public UpdateCreditCardData CreditCardToUpdate { get; set; }
    }
}

//  ##########################################################################
