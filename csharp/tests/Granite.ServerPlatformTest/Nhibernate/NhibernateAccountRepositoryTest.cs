//  ##########################################################################
//  # File Name: NhibernateAccountRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Payment;
using Granite.ServerPlatform.Bootstrap;
using Ninject;
using NUnit.Framework;
using Strata.Ninject.Bootstrap;

namespace Granite.ServerPlatform.Nhibernate
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [TestFixture]
    public
    class NhibernateAccountRepositoryTest:
        AccountRepositoryTest
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
