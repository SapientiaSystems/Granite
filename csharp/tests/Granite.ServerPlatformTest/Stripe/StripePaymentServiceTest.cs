//  ##########################################################################
//  # File Name: StripePaymentServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.RequiredService.Payment;
using Granite.ServerPlatform.Bootstrap;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Injection;
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
        protected override IContainer 
        CreateContainer()
        {
            IBootstrapper bootstrapper = new Bootstrapper();

            bootstrapper.Run(new MockApplicationFactory());

            return bootstrapper.Container;
        }
    }
}

//  ##########################################################################
