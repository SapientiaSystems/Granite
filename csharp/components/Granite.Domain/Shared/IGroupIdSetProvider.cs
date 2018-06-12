//  ##########################################################################
//  # File Name: IGroupIdSetProvider.cs
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
    interface IGroupIdSetProvider
    {
        ISet<MultiTenantId<string>>
        GetGroupIds();
    }
}

//  ##########################################################################
