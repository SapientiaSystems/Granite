//  ##########################################################################
//  # File Name: CreateAccountReply.cs
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
    class CreateAccountReply:
        GraniteReply
    {
        [DataMember]
        public AccountData NewAccount { get; set; }
    }
}

//  ##########################################################################
