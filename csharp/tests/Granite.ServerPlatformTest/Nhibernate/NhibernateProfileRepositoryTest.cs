//  ##########################################################################
//  # File Name: NhibernateProfileRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Profile;
using Granite.Domain.Session;
using Granite.ServerPlatform.Bootstrap;
using Ninject;
using NUnit.Framework;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Injection;
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
    class NhibernateProfileRepositoryTest:
        ProfileRepositoryTest
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
