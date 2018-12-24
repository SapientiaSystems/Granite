//  ##########################################################################
//  # File Name: NhibernateTenantRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Tenant;
using Granite.ServerPlatform.Bootstrap;
using Ninject.Modules;
using NUnit.Framework;
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
    class NhibernateTenantRepositoryTest:
        TenantRepositoryTest
    {
        protected override INinjectModule 
        GetPersistenceModule()
        {
            return 
                new GraniteServerPlatformModule<GraniteInitializer>();
        }

    }
}

//  ##########################################################################
