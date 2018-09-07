//  ##########################################################################
//  # File Name: GraniteServerPlatformModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Access;
using Granite.Domain.Persistence;
using Granite.RequiredService.Payment;
using Granite.ServerPlatform.Bootstrap;
using Granite.ServerPlatform.Stripe;
using Granite.ServerPlatform.WebApi;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Injection;
using Strata.Nhibernate.UnitOfWork;
using SystemEx.Injection;

namespace Granite.ServerPlatform.Nhibernate
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class GraniteServerPlatformModule<T>:
        PersistenceModule
        where T: IApplicationInitializer
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        GraniteServerPlatformModule():
            base("GraniteServerPlatformModule") {}

        public override void
        Initialize()
        {
            base.Initialize();

            Bind<IClientIpAddressRecorder>()
                .ToType<WebHostedClientIpAddressRecorder>()
                .WithScope(DefaultScope);

            Bind<IStripeContext>()
                .ToType<StripeContext>()
                .WithScope(DefaultScope);

            Bind<IExternalPlanProcessor>()
                .ToType<StripePlanProcessor>()
                .WithScope(DefaultScope);

            Bind<IExternalAccountProcessor>()
                .ToType<StripeAccountProcessor>()
                .WithScope(DefaultScope);

            Bind<IExternalSubscriptionProcessor>()
                .ToType<StripeSubscriptionProcessor>()
                .WithScope(DefaultScope);

            Bind<IExternalPaymentService>()
                .ToType<StripePaymentService>()
                .WithScope(DefaultScope);

            Bind<IApplicationInitializer>()
                .ToType<T>()
                .WithScope(DefaultScope);
        }
        
        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override IProvider<IUnitOfWorkProvider> 
        GetUnitOfWorkProviderProvider()
        {
            return
                new FuncBasedProvider<IUnitOfWorkProvider>(
                    () => new NhibernateUnitOfWorkProvider(
                        new string[] {"Granite.ServerPlatform"} ) );
        }
    }
}

//  ##########################################################################
