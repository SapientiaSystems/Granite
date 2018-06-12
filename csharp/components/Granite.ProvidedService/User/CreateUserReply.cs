//  ##########################################################################
//  # File Name: CreateUserReply.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Transfer;
using System.Runtime.Serialization;

namespace Granite.ProvidedService.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [DataContract]
    public
    class CreateUserReply:
        GraniteReply
    {
        [DataMember]
        public UserData  NewUser { get; set; }
    }
}

//  ##########################################################################
