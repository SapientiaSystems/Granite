//  ##########################################################################
//  # File Name: CreateProfileReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Transfer;
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
    class CreateProfileReply:
        GraniteReply
    {
        [DataMember]
        public ProfileData NewProfile { get; set; }
    }
}

//  ##########################################################################
