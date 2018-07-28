//  ##########################################################################
//  # File Name: SubscriptionRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Value;
using Granite.Domain.Configuration;
using Granite.Domain.Constraint;
using Granite.Domain.Role;
using Granite.Domain.Session;
using Granite.Domain.Shared;
using Granite.Domain.User;
using NUnit.Framework;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Injection;
using Strata.Ninject.Injection;
using System;
using System.Collections.Generic;

namespace Granite.Domain.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [TestFixture]
    public abstract
    class SubscriptionRepositoryTest
    {
        private ISubscriptionRepository itsTarget;
        private IUserRepository         itsUserRepository;
        private ISet<Guid>              itsSubscriptionIds;
        private ISet<Guid>              itsUserIds;
        private ITestUserGenerator      itsGenerator;

        protected string UserName { get; set; }

        [SetUp]
        public virtual void
        SetUp()
        {
            AbstractModule.DefaultScope = typeof(ThreadScope);

            IContainer container = CreateContainer();

            itsTarget = container.GetInstance<ISubscriptionRepository>();
            itsUserRepository = container.GetInstance<IUserRepository>();
            itsGenerator = container.GetInstance<ITestUserGenerator>();
            itsSubscriptionIds = new HashSet<Guid>();
            itsUserIds = new HashSet<Guid>();
            InsertUsers();
        }

        [TearDown]
        public virtual void
        TearDown()
        {
            IUnitOfWork unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            foreach (Guid id in itsSubscriptionIds)
            {
                if (itsTarget.HasUnique(id))
                    itsTarget.Remove(itsTarget.GetUnique(id));
            }
            unitOfWork.Commit();

            RemoveUsers();
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
            IUser user = itsUserRepository.GetUniqueWithUserName(UserName);
            ISubscription expected =
                new StandardSubscription()
                {
                    Created = DateTime.Now,
                    LastModified = DateTime.Now,
                    UserId = user.UserId,
                    ExternalSubscriptionId = itsGenerator.GenerateExternalUserId(),
                    ExternalPlanId = itsGenerator.GenerateExternalUserId()
                };
            ISubscription actual = itsTarget.Insert(expected);

            unitOfWork.Commit();

            itsSubscriptionIds.Add(actual.PrimaryId);

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.PrimaryId);
            Assert.AreNotEqual(Guid.Empty,actual.UserId);
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastModified,actual.LastModified);
            Assert.AreEqual(expected.ExternalSubscriptionId,actual.ExternalSubscriptionId);
            Assert.AreEqual(expected.ExternalPlanId,actual.ExternalPlanId);
        }

        [Test]
        public virtual void
        TestUpdate()
        {
            IUnitOfWork unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();
            IUser user = itsUserRepository.GetUniqueWithUserName(UserName);
            ISubscription expected =
                new StandardSubscription()
                {
                    Created = DateTime.Now,
                    LastModified = DateTime.Now,
                    UserId = user.UserId,
                    ExternalSubscriptionId = itsGenerator.GenerateExternalUserId(),
                    ExternalPlanId = itsGenerator.GenerateExternalUserId()
                };
            ISubscription actual = null;

            expected = itsTarget.Insert(expected);

            unitOfWork.Commit();
            unitOfWork = itsTarget.Provider.GetUnitOfWork();

            itsSubscriptionIds.Add(expected.PrimaryId);


            actual = itsTarget.Update(expected);

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.PrimaryId);
            Assert.AreNotEqual(Guid.Empty,actual.UserId);
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastModified,actual.LastModified);
            Assert.AreEqual(expected.ExternalSubscriptionId,actual.ExternalSubscriptionId);
            Assert.AreEqual(expected.ExternalPlanId,actual.ExternalPlanId);
        }

        protected void
        InsertUsers()
        {
            IUnitOfWork unitOfWork =
                itsUserRepository
                    .Provider
                    .GetUnitOfWork();

            IUser user = itsGenerator.GenerateUser();

            UserName = user.UserName;
            itsUserIds.Add(itsUserRepository.Insert(user).UserId);

            unitOfWork.Commit();
        }

        protected void
        RemoveUsers()
        {
            IUnitOfWork unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            foreach (Guid id in itsUserIds)
            {
                if (itsUserRepository.HasUnique(id))
                    itsUserRepository.Remove(
                        itsUserRepository.GetUnique(id));
                else
                    Assert.Warn(
                        string.Format(
                            "User {0} not found - cannot delete.",id));
            }

            unitOfWork.Commit();

        }


        protected abstract IContainer
        CreateContainer();


    }
}

//  ##########################################################################
