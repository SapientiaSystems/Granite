//  ##########################################################################
//  # File Name: IAccessTokenProvider.cs
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
    interface IAccessTokenProvider
    {
        string AccessToken { get; }
    }
}

//  ##########################################################################
