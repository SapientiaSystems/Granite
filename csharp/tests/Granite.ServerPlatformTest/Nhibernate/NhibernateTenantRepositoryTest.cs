//  ##########################################################################
//  # File Name: NhibernateTenantRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Tenant;
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
        protected override IModule 
        GetPersistenceModule()
        {
            return new GraniteServerPlatformModule();
        }

    }
}

//  ##########################################################################
