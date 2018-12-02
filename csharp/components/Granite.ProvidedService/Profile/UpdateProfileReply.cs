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
    class UpdateProfileReply:
        GraniteReply
    {
        [DataMember]
        public ProfileData UpdatedProfile { get; set; }
    }
}

//  ##########################################################################
