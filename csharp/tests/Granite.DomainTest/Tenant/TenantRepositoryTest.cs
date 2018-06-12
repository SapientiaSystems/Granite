//  ##########################################################################
//  # File Name: TenantRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Configuration;
using Granite.Domain.Constraint;
using Granite.Domain.Role;
using Granite.Domain.User;
using NUnit.Framework;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Injection;
using Strata.Foundation.Value;
using Strata.Ninject.Injection;
using System;
using System.Collections.Generic;

namespace Granite.Domain.Tenant
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [TestFixture]
    public abstract
    class TenantRepositoryTest
    {
        private ITenantRepository  itsTarget;
        private ISet<Guid>         itsIds;

        [SetUp]
        public virtual void
        SetUp()
        {
            AbstractModule.DefaultScope = typeof(ThreadScope);

            IContainer container = 
                new NinjectContainer(
                    GetPersistenceModule(),
                    new GraniteDomainModule<
                        GraniteRolesProvider,
                        GraniteConstraintsProvider,
                        GraniteUserFactory>());

            itsTarget = container.GetInstance<ITenantRepository>();
            itsIds = new HashSet<Guid>();
        }

        [TearDown]
        public virtual void
        TearDown()
        {
            IUnitOfWork unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            foreach (Guid id in itsIds)
            {
                if (itsTarget.HasUnique(id))
                    itsTarget.Remove(itsTarget.GetUnique(id));
            }

            unitOfWork.Commit();
            itsTarget = null;
        }

        [Test]
        public virtual void
        TestInsert()
        {
            IUnitOfWork unitOfWork = 
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            ITenant expected =
                new StandardTenant()
                {
                    TenantName = "Tenant1",
                    SecretKey = new HashedString("foobar")
                };
            ITenant actual = itsTarget.Insert(expected);

            unitOfWork.Commit();

            itsIds.Add(actual.PrimaryId);

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.PrimaryId);
            Assert.AreEqual(actual.PrimaryId,actual.TenantId);
            Assert.IsTrue(actual.SecretKey.Matches("foobar"));
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastModified,actual.LastModified);
        }

        [Test]
        public virtual void
        TestUpdate()
        {
            IUnitOfWork unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            ITenant expected =
                new StandardTenant()
                {
                    TenantName = "Tenant2",
                    SecretKey = new HashedString("foobar")
                };
            ITenant actual = null;

            expected = itsTarget.Insert(expected);

            unitOfWork.Commit();
            unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            itsIds.Add(expected.PrimaryId);
            expected.SecretKey = new HashedString("foo");
            actual = itsTarget.Update(expected);

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.PrimaryId);
            Assert.AreEqual(actual.PrimaryId,actual.TenantId);
            Assert.IsTrue(actual.SecretKey.Matches("foo"));
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastModified,actual.LastModified);
        }

        [Test]
        public virtual void
        TestGetUnique()
        {
            IUnitOfWork unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            ITenant expected =
                new StandardTenant()
                {
                    TenantName = "Tenant3",
                    SecretKey = new HashedString("foobar")
                };
            ITenant actual = null;
                
            expected = itsTarget.Insert(expected);

            unitOfWork.Commit();

            unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            itsIds.Add(expected.PrimaryId);

            actual = itsTarget.GetUnique(expected.PrimaryId);

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.PrimaryId);
            Assert.AreEqual(actual.PrimaryId,actual.TenantId);
            Assert.IsTrue(actual.SecretKey.Matches("foobar"));
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastModified,actual.LastModified);
        }

        [Test]
        public virtual void
        TestGetUniqueWithTenantName()
        {
            IUnitOfWork unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            ITenant expected =
                new StandardTenant()
                {
                    TenantName = "Tenant4",
                    SecretKey = new HashedString("foobar")
                };
            ITenant actual = null;

            expected = itsTarget.Insert(expected);

            unitOfWork.Commit();

            unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            itsIds.Add(expected.PrimaryId);

            actual = itsTarget.GetUniqueWithTenantName(expected.TenantName);

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.PrimaryId);
            Assert.AreEqual(actual.PrimaryId,actual.TenantId);
            Assert.IsTrue(actual.SecretKey.Matches("foobar"));
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastModified,actual.LastModified);
        }


        [Test]
        public virtual void
        TestHasUniqueWithTenantName()
        {
            IUnitOfWork unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            ITenant expected =
                new StandardTenant()
                {
                    TenantName = "Tenant4",
                    SecretKey = new HashedString("foobar")
                };
            ITenant actual = null;

            Assert.IsFalse(itsTarget.HasUniqueWithTenantName(expected.TenantName));

            expected = itsTarget.Insert(expected);

            unitOfWork.Commit();

            unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            itsIds.Add(expected.PrimaryId);

            Assert.IsTrue(itsTarget.HasUniqueWithTenantName(expected.TenantName));
        }

        protected abstract IModule
        GetPersistenceModule();


    }
}

//  ##########################################################################
