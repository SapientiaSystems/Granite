//  ##########################################################################
//  # File Name: GraniteRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Access;
using Strata.Foundation.Transfer;
using System;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Transfer
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class GraniteRequest:
        ServiceRequest,
        IAccessTokenProvider,
        ISessionIdProvider
    {
        [DataMember]
        public Guid   SessionId { get; set; }
        [DataMember]
        public string AccessToken { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected
        GraniteRequest()
        {
            SessionId = Guid.Empty;
        }
    }
}

//  ##########################################################################
