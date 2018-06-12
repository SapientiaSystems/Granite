//  ##########################################################################
//  # File Name: IGroupIdProvider.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Tenant;
using System;

namespace Granite.Domain.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IGroupIdProvider
    {
        MultiTenantId<string> GroupId { get; }
    }
}

//  ##########################################################################
