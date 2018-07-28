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
        public bool GetProfileOfCurrentUser { get; set; }
        [DataMember]
        public Guid ProfileIdOfProfileToGet { get; set; }
        [DataMember]
        public Guid UserIdOfProfileToGet { get; set; }

        public
        GetProfileRequest()
        {
            GetProfileOfCurrentUser = false;
            ProfileIdOfProfileToGet = Guid.Empty;
            UserIdOfProfileToGet = Guid.Empty;
        }
    }
}

//  ##########################################################################
