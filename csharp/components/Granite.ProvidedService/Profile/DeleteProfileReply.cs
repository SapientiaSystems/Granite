//  ##########################################################################
//  # File Name: UpdateProfileReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Transfer;
using System;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class DeleteProfileReply:
        GraniteReply
    {
        [DataMember]
        public Guid DeletedProfileId { get; set; }
    }
}

//  ##########################################################################
