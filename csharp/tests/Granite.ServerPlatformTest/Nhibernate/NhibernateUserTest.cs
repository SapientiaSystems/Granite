//  ##########################################################################
//  # File Name: NhibernateUserTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.User;
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
    class NhibernateUserTest:
        UserTest
    {
        protected override IModule 
        GetPersistenceModule()
        {
            return new NhibernateModule();
        }

    }
}

//  ##########################################################################
