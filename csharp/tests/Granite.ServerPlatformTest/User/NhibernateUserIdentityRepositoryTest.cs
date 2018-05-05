//  ##########################################################################
//  # File Name: NhibernateUserIdentityRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.User;
using Granite.ServerPlatform.Nhibernate;
using NHibernate.Cfg;
using NHibernate.Tool.hbm2ddl;
using NUnit.Framework;
using Strata.Foundation.Injection;
using System;

namespace Granite.ServerPlatform.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [TestFixture]
    public
    class NhibernateUserIdentityRepositoryTest:
        UserIdentityRepositoryTest
    {
        protected override IModule 
        GetPersistenceModule()
        {
            return new NhibernateModule();
        }

        //[Ignore("Use when needed")]
        [Test]
        public void
        GenerateSchema()
        {
            Configuration config = new Configuration();

            config
                .Configure()
                .AddAssembly("Granite.Domain")
                .AddAssembly("Granite.ServerPlatform");

            new SchemaExport(config).Create(true,true);

        }
    }
}

//  ##########################################################################
