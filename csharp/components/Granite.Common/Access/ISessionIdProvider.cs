//  ##########################################################################
//  # File Name: ISessionIdProvider.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;

namespace Granite.Common.Access
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface ISessionIdProvider
    {
        Guid SessionId { get; }
    }
}

//  ##########################################################################
