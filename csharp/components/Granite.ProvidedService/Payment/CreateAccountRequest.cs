//  ##########################################################################
//  # File Name: CreateAccountRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Transfer;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class CreateAccountRequest:
        GraniteRequest
    {
        [DataMember]
        public CreateAccountData AccountToCreate { get; set; }
    }
}

//  ##########################################################################
