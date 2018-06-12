//  ##########################################################################
//  # File Name: IUserMapper.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.User;
using Granite.ProvidedService.User;
using System;

namespace Granite.Application.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IUserMapper
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        UserData
        Map(IUser user);
    }
}

//  ##########################################################################
