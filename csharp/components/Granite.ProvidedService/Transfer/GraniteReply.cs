//  ##########################################################################
//  # File Name: GraniteReply.cs
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
    class GraniteReply:
        ServiceReply,
        ISessionIdProvider
    {
        [DataMember]
        public Guid   SessionId { get; set; }
        [DataMember]
        public string RefreshedAccessToken { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected
        GraniteReply()
        {
            RefreshedAccessToken = null;
        }
    }
}

//  ##########################################################################
