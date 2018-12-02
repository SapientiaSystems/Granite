//  ##########################################################################
//  # File Name: InitialAccountData.cs
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
    class InitialAccountData
    {
        [DataMember]
        public Guid                 UserIdOfAccountToCreate { get; set; }
        [DataMember]
        public string               Email { get; set; }
        [DataMember]
        public string               Description { get; set; }
        [DataMember]
        public InitialCreditCardData CreditCardToCreate { get; set; }
    }
}

//  ##########################################################################
