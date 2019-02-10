//  ##########################################################################
//  # File Name: NhibernatePaymentServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Payment;
using Granite.ServerPlatform.Bootstrap;
using Ninject;
using Strata.Ninject.Bootstrap;

namespace Granite.ServerPlatform.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class NhibernatePaymentServiceTest:
        PaymentServiceTest
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
