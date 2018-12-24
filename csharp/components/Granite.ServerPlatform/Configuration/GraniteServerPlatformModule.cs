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
using Ninject.Activation;
using Strata.Domain.UnitOfWork;
using Strata.Nhibernate.UnitOfWork;
using Strata.Ninject.Injection;
using System;
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
        GraniteServerPlatformModule() {}

        public override void
        Load()
        {
            base.Load();

            Bind<IClientIpAddressRecorder>()
                .To<WebHostedClientIpAddressRecorder>()
                .InDefaultScope();

            Bind<IStripeContext>()
                .To<StripeContext>()
                .InDefaultScope();

            Bind<IExternalPlanProcessor>()
                .To<StripePlanProcessor>()
                .InDefaultScope();

            Bind<IExternalAccountProcessor>()
                .To<StripeAccountProcessor>()
                .InDefaultScope();

            Bind<IExternalSubscriptionProcessor>()
                .To<StripeSubscriptionProcessor>()
                .InDefaultScope();

            Bind<IExternalPaymentService>()
                .To<StripePaymentService>()
                .InDefaultScope();

            Bind<IApplicationInitializer>()
                .To<T>()
                .InDefaultScope();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override Func<IContext,IUnitOfWorkProvider> 
        GetUnitOfWorkProviderProvider()
        {
            return
                (IContext context) => new NhibernateUnitOfWorkProvider(
                    new string[] {"Granite.ServerPlatform"} );
        }
    }
}

//  ##########################################################################
