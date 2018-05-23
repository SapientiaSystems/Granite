//  ##########################################################################
//  # File Name: NhibernateSessionRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Session;
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
    class NhibernateSessionRepositoryTest:
        SessionRepositoryTest
    {
        protected override IModule 
        GetPersistenceModule()
        {
            return new NhibernateModule();
        }

    }
}

//  ##########################################################################
