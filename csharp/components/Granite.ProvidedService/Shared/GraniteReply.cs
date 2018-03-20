//  ##########################################################################
//  # File Name: GraniteReply.cs
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
    class GraniteReply
    {
        [DataMember]
        public long SentTimestamp { get; set; }

        [DataMember]
        public long ReceivedTimestamp { get; set; }

        [DataMember]
        public long ExecutionMilliseconds { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected
        GraniteReply()
        {
            SentTimestamp         = 0;
            ReceivedTimestamp     = 0;
            ExecutionMilliseconds = 0;
        }
    }
}

//  ##########################################################################
