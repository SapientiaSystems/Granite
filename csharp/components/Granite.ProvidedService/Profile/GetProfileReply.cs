//  ##########################################################################
//  # File Name: UpdateProfileReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Transfer;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class GetProfileReply:
        GraniteReply
    {
        [DataMember]
        public ProfileData RequestedProfile { get; set; }
    }
}

//  ##########################################################################
