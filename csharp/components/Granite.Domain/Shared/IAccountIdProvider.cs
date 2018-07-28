//  ##########################################################################
//  # File Name: IAccountIdProvider.cs
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
    interface IAccountIdProvider
    {
        Guid AccountId { get; }
    }
}

//  ##########################################################################
