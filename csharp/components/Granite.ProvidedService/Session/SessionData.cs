//  ##########################################################################
//  # File Name: SessionData.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.User;
using System;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class SessionData
    {
        [DataMember]
        public Guid     SessionId { get; set; }
        [DataMember]
        public Guid     UserId { get; set; }
        [DataMember]
        public string   AccessToken { get; set; }
        [DataMember]
        public UserData User { get; set; }
    }
}

//  ##########################################################################
