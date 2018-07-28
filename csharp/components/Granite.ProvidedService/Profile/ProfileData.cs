//  ##########################################################################
//  # File Name: ProfileData.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

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
    class ProfileData
    {
        [DataMember]
        public Guid ProfileId { get; set; }
        [DataMember]
        public Guid UserId { get; set; }
    }
}

//  ##########################################################################
