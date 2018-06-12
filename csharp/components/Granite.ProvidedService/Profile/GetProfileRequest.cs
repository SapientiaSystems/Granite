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
    class GetProfileRequest:
        GraniteRequest
    {
        [DataMember]
        public bool ProfileOfCurrentUser { get; set; }
        [DataMember]
        public Guid ProfileIdToGet { get; set; }
        [DataMember]
        public Guid UserIdOfProfileToGet { get; set; }

        public
        GetProfileRequest()
        {
            ProfileOfCurrentUser = false;
            ProfileIdToGet = Guid.Empty;
            UserIdOfProfileToGet = Guid.Empty;
        }
    }
}

//  ##########################################################################
