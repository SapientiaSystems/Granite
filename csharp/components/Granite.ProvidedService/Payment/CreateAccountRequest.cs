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
        public string Email { get; set; }

        [DataMember]
        public string Country { get; set; }
    }
}

//  ##########################################################################
