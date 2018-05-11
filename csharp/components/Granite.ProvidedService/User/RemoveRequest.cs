//  ##########################################################################
//  # File Name: RemoveRequest.cs
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
    class RemoveRequest:
        GraniteRequest
    {
        [DataMember]
        public Guid   UserIdToRemove { get; set; }
        [DataMember]
        public string UserNameToRemove { get; set; }
    }
}

//  ##########################################################################
