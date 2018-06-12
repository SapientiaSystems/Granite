//  ##########################################################################
//  # File Name: IUserIdSetProvider.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using System.Collections.Generic;

namespace Granite.Domain.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface IUserIdSetProvider
    {
        ISet<Guid> 
        GetUserIds();
    }
}

//  ##########################################################################
