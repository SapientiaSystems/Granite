//  ##########################################################################
//  # File Name: NhibernateGroupRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Group;
using Granite.ServerPlatform.Bootstrap;
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
    class NhibernateGroupRepositoryTest:
        GroupRepositoryTest
    {
        protected override IModule 
        GetPersistenceModule()
        {
            return new GraniteServerPlatformModule<GraniteInitializer>();
        }

    }
}

//  ##########################################################################
