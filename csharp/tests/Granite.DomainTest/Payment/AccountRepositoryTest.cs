//  ##########################################################################
//  # File Name: AccountRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Granite.Domain.User;
using Ninject;
using NUnit.Framework;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Injection;
using Strata.Ninject.Injection;
using System;
using System.Collections.Generic;
using AbstractModule = Strata.Ninject.Injection.AbstractModule;

namespace Granite.Domain.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [TestFixture]
    public abstract
    class AccountRepositoryTest
    {
        private IAccountRepository itsTarget;
        private IUserRepository    itsUserRepository;
        private ISet<Guid>         itsAccountIds;
        private ISet<Guid>         itsUserIds;
        private ITestUserGenerator itsGenerator;

        protected string UserName { get; set; }

        [SetUp]
        public virtual void
        SetUp()
        {
            AbstractModule.DefaultScope = typeof(ThreadScope);

            IKernel container = CreateContainer();

            itsTarget = container.Get<IAccountRepository>();
            itsUserRepository = container.Get<IUserRepository>();
            itsGenerator = container.Get<ITestUserGenerator>();
            itsAccountIds = new HashSet<Guid>();
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

            foreach (Guid id in itsAccountIds)
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
            IAccount expected =
                new StandardAccount()
                {
                    Created = DateTime.Now,
                    LastModified = DateTime.Now,
                    UserId = user.UserId,
                    ExternalAccountId = itsGenerator.GenerateExternalUserId()
                };
            IAccount actual = itsTarget.Insert(expected);

            unitOfWork.Commit();

            itsAccountIds.Add(actual.PrimaryId);

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.PrimaryId);
            Assert.AreNotEqual(Guid.Empty,actual.UserId);
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastModified,actual.LastModified);
            Assert.AreEqual(expected.ExternalAccountId,actual.ExternalAccountId);
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
            IAccount expected =
                new StandardAccount()
                {
                    Created = DateTime.Now,
                    LastModified = DateTime.Now,
                    UserId = user.UserId,
                    ExternalAccountId = itsGenerator.GenerateExternalUserId()
                };
            IAccount actual = null;
            
            expected = itsTarget.Insert(expected);

            unitOfWork.Commit();
            unitOfWork = itsTarget.Provider.GetUnitOfWork();

            itsAccountIds.Add(expected.PrimaryId);

            expected.ExternalAccountId = "xxxxx-yyyyyyyyy-zzzzzzz";

            actual = itsTarget.Update(expected);

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.PrimaryId);
            Assert.AreNotEqual(Guid.Empty,actual.UserId);
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastModified,actual.LastModified);
            Assert.AreEqual(expected.ExternalAccountId,actual.ExternalAccountId);
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

        protected abstract IKernel
        CreateContainer();
    }
}

//  ##########################################################################
