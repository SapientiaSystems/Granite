//  ##########################################################################
//  # File Name: NhibernateAccountRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Payment;
using Granite.ServerPlatform.Bootstrap;
using NUnit.Framework;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Injection;

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
