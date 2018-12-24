//  ##########################################################################
//  # File Name: IAuthorizationPropertySupplier.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Shared;
using Granite.Application.Tenant;
using Granite.Common.Access;

namespace Granite.Application.Interception
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface IAuthorizationPropertySupplier
    {
        ITenantManager           Manager { get; }
        IAccessTokenRefresher    Refresher { get; }
        IClientIpAddressRecorder Recorder { get; }
    }
}

//  ##########################################################################
