//  ##########################################################################
//  # File Name: NhibernateProfileRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Profile;
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
    class NhibernateProfileRepositoryTest:
        ProfileRepositoryTest
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
