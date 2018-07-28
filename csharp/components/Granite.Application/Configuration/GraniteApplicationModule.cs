//  ##########################################################################
//  # File Name: GraniteApplicationModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using AutoMapper;
using Granite.Application.Access;
using Granite.Application.Mapper;
using Granite.Application.Payment;
using Granite.Application.Profile;
using Granite.Application.Registration;
using Granite.Application.Session;
using Granite.Application.Shared;
using Granite.Application.Tenant;
using Granite.Application.User;
using Granite.Common.Access;
using Granite.Domain.Profile;
using Granite.ProvidedService.Payment;
using Granite.ProvidedService.Profile;
using Granite.ProvidedService.Registration;
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
            Bind<IMapper>()
                .ToProvider<MapperProvider>()
                .WithScope(DefaultScope);

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

            Bind<ISessionProcessor>()
                .ToType<SessionProcessor>()
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

            Bind<IProfileProcessor>()
                .ToType<ProfileProcessor>()
                .WithScope(DefaultScope);

            Bind<IProfileService>()
                .ToType<ProfileService>()
                .WithScope(DefaultScope);

            Bind<IPaymentProcessor>()
                .ToType<PaymentProcessor>()
                .WithScope(DefaultScope);

            Bind<IPaymentService>()
                .ToType<PaymentService>()
                .WithScope(DefaultScope);

            Bind<IRegistrationProcessor>()
                .ToType<RegistrationProcessor>()
                .WithScope(DefaultScope);

            Bind<IRegistrationService>()
                .ToType<RegistrationService>()
                .WithScope(DefaultScope);

        }
    }
}

//  ##########################################################################
