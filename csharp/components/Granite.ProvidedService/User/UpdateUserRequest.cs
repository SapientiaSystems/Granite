//  ##########################################################################
//  # File Name: UpdateUserRequest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

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
    class UpdateUserRequest
    {
        [DataMember]
        public UpdateUserData UserToUpdate { get; set; }
    }
}

//  ##########################################################################
