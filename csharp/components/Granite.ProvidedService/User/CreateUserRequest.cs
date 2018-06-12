//  ##########################################################################
//  # File Name: CreateUserRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Transfer;
using Strata.Foundation.Value;
using System;
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
    class CreateUserRequest:
        GraniteRequest
    {
        [DataMember]
        public CreateUserData UserToCreate { get; set; }
    }
}

//  ##########################################################################
