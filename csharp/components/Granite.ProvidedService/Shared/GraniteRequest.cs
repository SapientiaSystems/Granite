//  ##########################################################################
//  # File Name: GraniteRequest.cs
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
    class GraniteRequest:
        ServiceRequest
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
        GraniteRequest()
        {
            SessionId = Guid.Empty;
            UserId    = Guid.Empty;
        }
    }
}

//  ##########################################################################
