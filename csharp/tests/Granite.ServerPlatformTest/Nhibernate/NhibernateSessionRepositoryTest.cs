//  ##########################################################################
//  # File Name: NhibernateSessionRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Session;
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
    class NhibernateSessionRepositoryTest:
        SessionRepositoryTest
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
