//  ##########################################################################
//  # File Name: GetOrLoginReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Transfer;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [DataContract]
    public
    class GetOrLoginReply:
        ServiceReply
    {
        [DataMember]
        public SessionData Session { get; set; }
    }
}

//  ##########################################################################
