//  ##########################################################################
//  # File Name: GetUserReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Transfer;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [DataContract]
    public
    class GetUserReply:
        GraniteReply
    {
        [DataMember]
        public UserData RequestedUser { get; set; }
    }
}

//  ##########################################################################
