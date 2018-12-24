//  ##########################################################################
//  # File Name: GroupRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Configuration;
using Granite.Domain.Constraint;
using Granite.Domain.Role;
using Granite.Domain.Shared;
using Granite.Domain.Tenant;
using Granite.Domain.User;
using NUnit.Framework;
using Strata.Domain.UnitOfWork;
using Strata.Ninject.Injection;
using Strata.Foundation.Value;
using System;
using System.Collections.Generic;
using System.Linq;
using Ninject;
using Strata.Foundation.Injection;
using AbstractModule = Strata.Ninject.Injection.AbstractModule;
using Ninject.Modules;

namespace Granite.Domain.Group
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [TestFixture]
    public abstract
    class GroupRepositoryTest
    {
        private ITenantRepository           tenants;
        private ITenant                     tenant;
        private IGroupRepository            itsTarget;
        private ISet<MultiTenantId<string>> itsIds;

        [SetUp]
        public virtual void
        SetUp()
        {
            AbstractModule.DefaultScope = typeof(ThreadScope);

            IKernel container = 
                new StandardKernel(
                    GetPersistenceModule(),
                    new GraniteDomainModule<
                        GraniteRolesProvider,
                        GraniteConstraintsProvider,
                        GraniteUserFactory>());

            tenants = container.Get<ITenantRepository>();
            tenant = new StandardTenant("TestTenant",new HashedString("foobar"));
            itsTarget = container.Get<IGroupRepository>();
            itsIds = new HashSet<MultiTenantId<string>>();

            tenant = tenants.Insert(tenant);
            tenant.Activate();
            itsIds.Add(itsTarget.Insert(new StandardGroup("Foo")).MultiTenantPrimaryId);
            itsIds.Add(itsTarget.Insert(new StandardGroup("Bar")).MultiTenantPrimaryId);
        }

        [TearDown]
        public virtual void
        TearDown()
        {
            IUnitOfWork unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            foreach (MultiTenantId<string> id in itsIds)
            {
                if (itsTarget.HasUnique(id))
                    itsTarget.Remove(itsTarget.GetUnique(id));
            }

            tenants.Remove(tenant);
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
            IGroup foo = itsTarget.GetUnique("Foo");
            IGroup bar = itsTarget.GetUnique("Bar");
            IGroup expected = new StandardGroup("FooBar");

            expected
                .MakeMember(new MockUserIdProvider())
                .MakeMember(new MockUserIdProvider())
                .MakeMember(new MockUserIdProvider())
                .MakeMember(new MockUserIdProvider())
                .MakeMember(new MockUserIdProvider())
                .IncludeGroup(foo)
                .IncludeGroup(bar);

            IGroup actual = itsTarget.Insert(expected);

            unitOfWork.Commit();

            itsIds.Add(actual.MultiTenantPrimaryId);

            Assert.NotNull(actual);
            Assert.AreEqual(tenant.TenantId,actual.TenantId);
            Assert.NotNull(actual.PrimaryId);
            Assert.AreEqual(expected.MultiTenantPrimaryId,actual.MultiTenantPrimaryId);
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastModified,actual.LastModified);
            Assert.AreEqual(2,actual.IncludedGroups.Count);
            Assert.IsTrue(actual.Includes(foo));
            Assert.IsTrue(actual.Includes(bar));
        }

        [Test]
        public virtual void
        TestUpdate()
        {
            IUnitOfWork unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            IGroup expected = new StandardGroup("FooBar");
            IGroup actual = null;
            IGroup foo = itsTarget.GetUnique("Foo");
            IGroup bar = itsTarget.GetUnique("Bar");

            expected = itsTarget.Insert(expected);
            unitOfWork.Commit();
            itsIds.Add(expected.MultiTenantPrimaryId);

            unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            expected
                .IncludeGroup(foo)
                .IncludeGroup(bar);

            Assert.AreEqual(2,expected.IncludedGroups.Count);
            Assert.IsTrue(expected.IncludedGroups.Any(g => g.PrimaryId.Equals("Foo")));
            Assert.IsTrue(expected.IncludedGroups.Any(g => g.PrimaryId.Equals("Bar")));
            actual = itsTarget.Update(expected);
            unitOfWork.Commit();

            Assert.NotNull(actual);
            Assert.AreEqual(2,actual.IncludedGroups.Count);
            Assert.IsTrue(actual.IncludedGroups.Contains(foo));
            Assert.IsTrue(actual.IncludedGroups.Contains(bar));
            Assert.AreNotEqual(Guid.Empty,actual.PrimaryId);
            Assert.AreEqual(expected.PrimaryId,actual.PrimaryId);
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastModified,actual.LastModified);
            Assert.IsTrue(actual.Includes(foo));
            Assert.IsTrue(actual.Includes(bar));
        }

        [Test]
        public virtual void
        TestGetUnique()
        {
            IUnitOfWork unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            IGroup expected = new StandardGroup("FooBar");

            IGroup actual = null;

            expected = itsTarget.Insert(expected);
            unitOfWork.Commit();

            itsIds.Add(expected.MultiTenantPrimaryId);
            actual = itsTarget.GetUnique(expected.PrimaryId);

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.PrimaryId);
            Assert.AreEqual(expected.PrimaryId,actual.PrimaryId);
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastModified,actual.LastModified);
        }

        protected abstract INinjectModule
        GetPersistenceModule();


    }
}

//  ##########################################################################
