//  ##########################################################################
//  # File Name: ActiveDirectoryModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.RequiredService.Authentication;
using Granite.RequiredService.User;
using Microsoft.Azure.ActiveDirectory.GraphClient;
using Microsoft.IdentityModel.Clients.ActiveDirectory;
using Strata.Foundation.Injection;
using System;
using System.Configuration;

namespace Granite.ServerPlatform.ActiveDirectory
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class ActiveDirectoryModule:
        AbstractModule
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        ActiveDirectoryModule(): 
            base("ActiveDirectoryModule") {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override void 
        Initialize()
        {

            Bind<ActiveDirectoryGraphClient>()
                .ToProvider(
                    new FuncBasedProvider<ActiveDirectoryGraphClient>(
                        () =>
                        {
                            string clientId = ConfigurationManager.AppSettings["ClientId"];
                            string appKey = ConfigurationManager.AppSettings["AppKey"];
                            string tenantId = ConfigurationManager.AppSettings["TenantId"];

                            return
                                new ActiveDirectoryGraphClient(clientId,appKey,tenantId);

                        }))
                .WithScope<RequestScope>();

            Bind<IExternalUserService>()
                .ToType<ActiveDirectoryUserService>()
                .WithScope<RequestScope>();

            Bind<IAuthenticationService>()
                .ToType<ActiveDirectoryAuthenticationService>()
                .WithScope<RequestScope>();
        }
    }
}

//  ##########################################################################
