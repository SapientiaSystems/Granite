//  ##########################################################################
//  # File Name: NhibernatePaymentServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.User;
using Granite.ServerPlatform.Bootstrap;
using Ninject;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Injection;
using Strata.Ninject.Bootstrap;

namespace Granite.ServerPlatform.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class NhibernateUserServiceTest:
        UserServiceTest
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
