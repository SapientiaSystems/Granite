//  ##########################################################################
//  # File Name: GetAccountReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Transfer;
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
    class GetAccountReply:
        GraniteReply
    {
        [DataMember]
        public AccountData RequestedAccount { get; set; }
    }
}

//  ##########################################################################
