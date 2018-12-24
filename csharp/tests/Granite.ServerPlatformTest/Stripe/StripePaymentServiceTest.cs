//  ##########################################################################
//  # File Name: StripePaymentServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.RequiredService.Payment;
using Granite.ServerPlatform.Bootstrap;
using Ninject;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Injection;
using Strata.Ninject.Bootstrap;
using System;

namespace Granite.ServerPlatform.Stripe
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class StripePaymentServiceTest:
        ExternalPaymentServiceTest
    {
        protected override IKernel 
        CreateContainer()
        {
            INinjectBootstrapper bootstrapper = new NinjectBootstrapper();

            bootstrapper.Run(new MockApplicationFactory());

            return bootstrapper.Container;
        }
    }
}

//  ##########################################################################
