//  ##########################################################################
//  # File Name: UserTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using NUnit.Framework;
using Strata.Foundation.Injection;
using Strata.Ninject.Injection;
using Strata.Domain.UnitOfWork;
using System;
using System.Collections.Generic;
using Granite.Domain.Role;
using Granite.Domain.Configuration;
using Granite.Domain.Constraint;
using Granite.Domain.Group;
using Granite.Domain.Tenant;
using AbstractModule = Strata.Ninject.Injection.AbstractModule;
using Ninject;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [TestFixture]
    public abstract
    class UserTest
    {
        private IUnitOfWorkProvider itsProvider;
        private ITenantRepository   itsTenants;
        private IUserRepository     itsUsers;
        private IRoleRepository     itsRoles;
        private IGroupRepository    itsGroups;
        private ISet<Guid>          itsIds;
        private ITestUserGenerator  itsGenerator;
        private IUser               user1;
        private IUser               user2;
        private IUser               user3;

        [SetUp]
        public virtual void
        SetUp()
        {
            AbstractModule.DefaultScope = typeof(ThreadScope);

            IKernel container = CreateContainer();
            IUnitOfWork unitOfWork = null;

            itsProvider = container.Get<IUnitOfWorkProvider>();
            itsTenants = container.Get<ITenantRepository>();
            itsUsers = container.Get<IUserRepository>();
            itsRoles = container.Get<IRoleRepository>();
            itsGroups = container.Get<IGroupRepository>();
            itsGenerator = container.Get<ITestUserGenerator>();

            itsIds = new HashSet<Guid>();

            unitOfWork = itsProvider.GetUnitOfWork();

            user1 = itsUsers.Insert(itsGenerator.GenerateUser());
            user2 = itsUsers.Insert(itsGenerator.GenerateUser());
            user3 = itsUsers.Insert(itsGenerator.GenerateUser());

            user2.AttachRole(itsRoles.GetRole("GraniteUser"));
            user3.AttachRole(itsRoles.GetRole("GraniteAdministrator"));

            user1 = itsUsers.Update(user1);
            user2 = itsUsers.Update(user2);
            user3 = itsUsers.Update(user3);

            unitOfWork.Commit();

            itsIds.Add(user1.PrimaryId);
            itsIds.Add(user2.PrimaryId);
            itsIds.Add(user3.PrimaryId);
        }

        [TearDown]
        public virtual void
        TearDown()
        {
            IUnitOfWork unitOfWork = itsProvider.GetUnitOfWork();

            foreach (Guid id in itsIds)
            {
                if (itsUsers.HasUnique(id))
                    itsUsers.Remove(itsUsers.GetUnique(id));
            }

            unitOfWork.Commit();
            itsUsers = null;
        }

        [Test]
        public virtual void
        TestGetRoles()
        {
            IUnitOfWork        unitOfWork = itsProvider.GetUnitOfWork();
            ICollection<IRole> roles1 = user1.GetRoles();
            ICollection<IRole> roles2 = user2.GetRoles();
            ICollection<IRole> roles3 = user3.GetRoles();

            Assert.AreEqual(1,roles1.Count);
            Assert.AreEqual(2,roles2.Count);
            Assert.AreEqual(2,roles3.Count);
        }

        protected abstract IKernel
        CreateContainer();


    }
}

//  ##########################################################################
