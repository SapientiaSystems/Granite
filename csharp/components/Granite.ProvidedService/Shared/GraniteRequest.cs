//  ##########################################################################
//  # File Name: GraniteRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.Runtime.Serialization;

namespace Granite.ProvidedService.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class GraniteRequest
    {
        [DataMember]
        public long SentTimestamp { get; set; }

        [DataMember]
        public long ReceivedTimestamp { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected
        GraniteRequest()
        {
            SentTimestamp = 0;
            ReceivedTimestamp = 0;
        }
    }
}

//  ##########################################################################
