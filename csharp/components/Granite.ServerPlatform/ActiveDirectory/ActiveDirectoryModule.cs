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
            Bind<AuthenticationContext>()
                .ToProvider(
                    new FuncBasedProvider<AuthenticationContext>(
                        () => null))
                .WithScope<RequestScope>();

            Bind<ActiveDirectoryClient>()
                .ToProvider(
                    new FuncBasedProvider<ActiveDirectoryClient>(
                        () => null))
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
