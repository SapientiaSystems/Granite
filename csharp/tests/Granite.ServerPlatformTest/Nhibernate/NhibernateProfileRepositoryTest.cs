//  ##########################################################################
//  # File Name: NhibernateProfileRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Profile;
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
    class NhibernateProfileRepositoryTest:
        ProfileRepositoryTest
    {
        protected override IModule 
        GetPersistenceModule()
        {
            return new NhibernateModule();
        }

    }
}

//  ##########################################################################
