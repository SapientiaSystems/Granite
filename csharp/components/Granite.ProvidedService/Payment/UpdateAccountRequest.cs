//  ##########################################################################
//  # File Name: UpdateAccountRequest.cs
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
    class UpdateAccountRequest:
        GraniteRequest
    {
        [DataMember]
        public UpdateAccountData AccountToUpdate { get; set; }
    }
}

//  ##########################################################################
