//  ##########################################################################
//  # File Name: ActiveDirectoryAuthenticationServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.RequiredService.Authentication;
using Granite.RequiredService.User;
using NUnit.Framework;
using System;
using System.Configuration;

namespace Granite.ServerPlatform.ActiveDirectory
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [Ignore("deprecated")]
    public
    class ActiveDirectoryAuthenticationServiceTest:
        AuthenticationServiceTest
    {
        protected override IAuthenticationService 
        CreateTarget()
        {
            string clientId = ConfigurationManager.AppSettings["ClientId"];
            string tenantId = ConfigurationManager.AppSettings["TenantId"];

            return
                new ActiveDirectoryAuthenticationService(clientId,tenantId);
        }

        protected override IExternalUserService 
        CreateUserService()
        {
            string clientId = ConfigurationManager.AppSettings["ClientId"];
            string appKey = ConfigurationManager.AppSettings["AppKey"];
            string tenantId = ConfigurationManager.AppSettings["TenantId"];

            return
                new ActiveDirectoryUserService(
                    new ActiveDirectoryGraphClient(clientId,appKey,tenantId));
        }
    }
}

//  ##########################################################################
