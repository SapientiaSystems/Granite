//  ##########################################################################
//  # File Name: CreateProfileRequest.cs
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
    class CreateProfileRequest:
        GraniteRequest
    {
        [DataMember]
        public CreateOrUpdateProfileData ProfileToCreate { get; set; }
    }
}

//  ##########################################################################
