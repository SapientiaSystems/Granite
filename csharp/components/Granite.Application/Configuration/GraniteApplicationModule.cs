//  ##########################################################################
//  # File Name: GraniteApplicationModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Access;
using Granite.Application.Payment;
using Granite.Application.Session;
using Granite.Application.Shared;
using Granite.Application.Tenant;
using Granite.Application.User;
using Granite.Common.Access;
using Granite.ProvidedService.Payment;
using Granite.ProvidedService.Session;
using Granite.ProvidedService.User;
using Strata.Foundation.Injection;

namespace Granite.Application.Configuration
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class GraniteApplicationModule:
        AbstractModule
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        public
        GraniteApplicationModule(): 
            base("GraniteApplicationModule") {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override void
        Initialize()
        {
            Bind<IAuthenticator>()
                .ToType<Authenticator>()
                .WithScope(DefaultScope);

            Bind<IAuthorizer>()
                .ToType<Authorizer>()
                .WithScope(DefaultScope);

            Bind<ITenantManager>()
                .ToType<TenantManager>()
                .WithScope(DefaultScope);

            Bind<IAccessTokenRefresher>()
                .ToType<AccessTokenRefresher>()
                .WithScope(DefaultScope);

            Bind<ISessionService>()
                .ToType<SessionService>()
                .WithScope(DefaultScope);

            Bind<IUserProcessor>()
                .ToType<UserProcessor>()
                .WithScope(DefaultScope);

            Bind<IUserService>()
                .ToType<UserService>()
                .WithScope(DefaultScope);

            Bind<IPaymentService>()
                .ToType<PaymentService>()
                .WithScope(DefaultScope);

        }
    }
}

//  ##########################################################################
