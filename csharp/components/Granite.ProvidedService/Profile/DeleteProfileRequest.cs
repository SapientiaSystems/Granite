//  ##########################################################################
//  # File Name: UpdateProfileRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Transfer;
using System;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [DataContract]
    public
    class DeleteProfileRequest:
        GraniteRequest
    {
        [DataMember]
        public Guid ProfileIdToDelete { get; set; }
        [DataMember]
        public Guid UserIdOfProfileToDelete { get; set; }
    }
}

//  ##########################################################################
