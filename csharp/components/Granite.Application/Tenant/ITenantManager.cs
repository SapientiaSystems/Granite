//  ##########################################################################
//  # File Name: ITenantManager.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Access;
using System;

namespace Granite.Application.Tenant
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface ITenantManager
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        ActivateTenant(
            IAccessTokenProvider tokenProvider,
            ISessionIdProvider   idProvider);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        DeactivateTenant(
            IAccessTokenProvider tokenProvider,
            ISessionIdProvider   idProvider);
    }
}

//  ##########################################################################
