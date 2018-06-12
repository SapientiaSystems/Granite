//  ##########################################################################
//  # File Name: ISubscriptionIdProvider.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;

namespace Granite.Domain.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface ISubscriptionIdProvider
    {
        Guid SubscriptionId { get; }
    }
}

//  ##########################################################################
