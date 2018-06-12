//  ##########################################################################
//  # File Name: UpdateProfileRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Transfer;
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
    class UpdateProfileRequest:
        GraniteRequest
    {
        [DataMember]
        public CreateOrUpdateProfileData ProfileToUpdate { get; set; }
    }
}

//  ##########################################################################
