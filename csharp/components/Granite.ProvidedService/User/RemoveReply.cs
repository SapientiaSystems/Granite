//  ##########################################################################
//  # File Name: RemoveReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Shared;
using System;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [DataContract]
    public
    class RemoveReply:
        GraniteReply
    {
        [DataMember]
        public Guid UserIdRemoved { get; set; }
    }
}

//  ##########################################################################
