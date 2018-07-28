//  ##########################################################################
//  # File Name: SessionRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Configuration;
using Granite.Domain.Constraint;
using Granite.Domain.Role;
using Granite.Domain.Shared;
using Granite.Domain.User;
using NUnit.Framework;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Injection;
using Strata.Ninject.Injection;
using System;
using System.Collections.Generic;

namespace Granite.Domain.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [TestFixture]
    public abstract
    class SessionRepositoryTest
    {
        private ISessionRepository itsTarget;
        private IUserRepository    itsUserRepository;
        private ISet<Guid>         itsSessionIds;
        private ISet<Guid>         itsUserIds;
        private ITestUserGenerator itsGenerator;

        protected string UserName1 { get; set; }
        protected string UserName2 { get; set; }
        protected string UserName3 { get; set; }

        [SetUp]
        public virtual void
        SetUp()
        {
            AbstractModule.DefaultScope = typeof(ThreadScope);

            IContainer container = CreateContainer();

            itsTarget = container.GetInstance<ISessionRepository>();
            itsUserRepository = container.GetInstance<IUserRepository>();
            itsGenerator = container.GetInstance<ITestUserGenerator>();
            itsSessionIds = new HashSet<Guid>();
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

            foreach (Guid id in itsSessionIds)
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
            IUser user = itsUserRepository.GetUniqueWithUserName(UserName1);
            ISession expected =
                new StandardSession()
                {
                    UserId = user.UserId,
                    Created = DateTime.Now,
                    LastRefresh = DateTime.Now,
                    LastRequest = DateTime.Now
                };
            ISession actual =
                itsTarget.Insert(expected);

            unitOfWork.Commit();

            itsSessionIds.Add(actual.PrimaryId);

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.PrimaryId);
            Assert.AreNotEqual(Guid.Empty,actual.UserId);
            Assert.NotNull(actual.User);
            Assert.IsTrue(actual.IsUserValid);
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastRefresh,actual.LastRefresh);
            Assert.AreEqual(expected.LastRequest,actual.LastRequest);
        }

        [Test]
        public virtual void
        TestUpdate()
        {
            IUnitOfWork unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();
            IUser user = itsUserRepository.GetUniqueWithUserName(UserName2);
            ISession expected =
                new StandardSession()
                {
                    UserId = user.UserId,
                    Created = DateTime.Now,
                    LastRefresh = DateTime.Now,
                    LastRequest = DateTime.Now
                };
            ISession actual =
                itsTarget.Insert(expected);

            unitOfWork.Commit();

            itsSessionIds.Add(actual.PrimaryId);

            unitOfWork = itsTarget.Provider.GetUnitOfWork();

            expected = actual;
            expected.LastRefresh = DateTime.Now;

            actual = itsTarget.Update(expected);
            unitOfWork.Commit();
            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.PrimaryId);
            Assert.AreNotEqual(Guid.Empty,actual.UserId);
            Assert.NotNull(actual.User);
            Assert.IsTrue(actual.IsUserValid);
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastRefresh,actual.LastRefresh);
            Assert.AreEqual(expected.LastRequest,actual.LastRequest);
        }

        protected void
        InsertUsers()
        {
            IUnitOfWork unitOfWork =
                itsUserRepository
                    .Provider
                    .GetUnitOfWork();

            IUser user1 = itsGenerator.GenerateUser();
            IUser user2 = itsGenerator.GenerateUser();
            IUser user3 = itsGenerator.GenerateUser();

            UserName1 = user1.UserName;
            UserName2 = user2.UserName;
            UserName3 = user3.UserName;

            itsUserIds.Add(itsUserRepository.Insert(user1).UserId);
            itsUserIds.Add(itsUserRepository.Insert(user2).UserId);
            itsUserIds.Add(itsUserRepository.Insert(user3).UserId);

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
