//  ##########################################################################
//  # File Name: CreateExternalAccountRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Payment;
using Strata.Foundation.Transfer;
using System.Runtime.Serialization;

namespace Granite.RequiredService.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class CreateExternalAccountRequest:
        ServiceRequest
    {
        [DataMember]
        public string             Description { get; set; }
        [DataMember]
        public InitialAccountData AccountToCreate { get; set; }
    }
}

//  ##########################################################################
