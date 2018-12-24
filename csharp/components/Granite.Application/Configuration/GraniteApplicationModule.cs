//  ##########################################################################
//  # File Name: GraniteApplicationModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using AutoMapper;
using Granite.Application.Access;
using Granite.Application.Interception;
using Granite.Application.Mapper;
using Granite.Application.Payment;
using Granite.Application.Profile;
using Granite.Application.Registration;
using Granite.Application.Session;
using Granite.Application.Shared;
using Granite.Application.Tenant;
using Granite.Application.User;
using Granite.Common.Access;
using Granite.ProvidedService.Payment;
using Granite.ProvidedService.Profile;
using Granite.ProvidedService.Registration;
using Granite.ProvidedService.Session;
using Granite.ProvidedService.User;
using Ninject.Activation;
using Ninject.Extensions.Interception.Infrastructure.Language;
using Strata.Application.Interception;
using Strata.Application.Messaging;
using Strata.Ninject.Injection;

namespace Granite.Application.Configuration
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class GraniteApplicationModule<M>:
        AbstractModule
        where M: IProvider<IMapper>,new()
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        public
        GraniteApplicationModule() {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override void
        Load()
        {
            Bind<IActionQueue>()
                .To<StandardActionQueue>()
                .InDefaultScope();

            Bind<IMapper>()
                .ToProvider<M>()
                .InDefaultScope();

            Bind<IAuthenticator>()
                .To<Authenticator>()
                .InDefaultScope();

            Bind<IAuthorizer>()
                .To<Authorizer>()
                .InDefaultScope();

            Bind<ITenantManager>()
                .To<TenantManager>()
                .InDefaultScope();

            Bind<IAccessTokenRefresher>()
                .To<AccessTokenRefresher>()
                .InDefaultScope();

            Bind<ISessionProcessor>()
                .To<SessionProcessor>()
                .InDefaultScope();

            Bind<ISessionService>()
                .To<SessionService>()
                .InDefaultScope()
                .Intercept()
                .With(new AuthorizedServiceInterceptor(3));

            Bind<IUserProcessor>()
                .To<UserProcessor>()
                .InDefaultScope();

            Bind<IUserService>()
                .To<UserService>()
                .InDefaultScope()
                .Intercept()
                .With(new AuthorizedServiceInterceptor(3));

            Bind<IProfileProcessor>()
                .To<ProfileProcessor>()
                .InDefaultScope();

            Bind<IProfileService>()
                .To<ProfileService>()
                .InDefaultScope()
                .Intercept()
                .With(new AuthorizedServiceInterceptor(3));

            Bind<IPaymentProcessor>()
                .To<PaymentProcessor>()
                .InDefaultScope();

            Bind<IPaymentService>()
                .To<PaymentService>()
                .InDefaultScope()
                .Intercept()
                .With(new AuthorizedServiceInterceptor(3));

            Bind<IRegistrationProcessor>()
                .To<RegistrationProcessor>()
                .InDefaultScope();

            Bind<IRegistrationService>()
                .To<RegistrationService>()
                .InDefaultScope()
                .Intercept()
                .With(new AuthorizedServiceInterceptor(3));
        }
    }
}

//  ##########################################################################
