//  ##########################################################################
//  # File Name: GraniteReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Service.Core;
using System;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class GraniteReply:
        ServiceReply
    {
        [DataMember]
        public Guid SessionId { get; set; }
        [DataMember]
        public Guid UserId { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected
        GraniteReply()
        {
        }
    }
}

//  ##########################################################################
