//  ##########################################################################
//  # File Name: ProfileRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

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

namespace Granite.Domain.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [TestFixture]
    public abstract
    class ProfileRepositoryTest
    {
        private IProfileRepository itsTarget;
        private IUserRepository    itsUserRepository;
        private ISet<Guid>         itsProfileIds;
        private ISet<Guid>         itsUserIds;
        private ITestUserGenerator itsGenerator;

        protected string UserName { get; set; }

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
                        GraniteUserFactory>(),
                    new TestGeneratorModule());

            itsTarget = container.GetInstance<IProfileRepository>();
            itsUserRepository = container.GetInstance<IUserRepository>();
            itsGenerator = container.GetInstance<ITestUserGenerator>();
            itsProfileIds = new HashSet<Guid>();
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

            foreach (Guid id in itsProfileIds)
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
            IProfile expected =
                new StandardProfile()
                {
                    Created = DateTime.Now,
                    LastModified = DateTime.Now,
                    UserId = user.UserId,
                    UserName = user.UserName,
                    Name = itsGenerator.GenerateName(),
                    Email = itsGenerator.GenerateEmail(),
                    Phone = itsGenerator.GeneratePhone(),
                    Address = itsGenerator.GenerateAddress(),
                    JobTitle = "Software Engineer"
                };
            IProfile actual = itsTarget.Insert(expected);

            unitOfWork.Commit();

            itsProfileIds.Add(actual.PrimaryId);

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.PrimaryId);
            Assert.AreNotEqual(Guid.Empty,actual.UserId);
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastModified,actual.LastModified);
            Assert.AreEqual(expected.Name,actual.Name);
            Assert.AreEqual(expected.Email,actual.Email);
            Assert.AreEqual(expected.Phone,actual.Phone);
            Assert.AreEqual(expected.Address,actual.Address);
            Assert.AreEqual(expected.JobTitle,actual.JobTitle);
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
            IProfile expected =
                new StandardProfile()
                {
                    Created = DateTime.Now,
                    LastModified = DateTime.Now,
                    UserId = user.UserId,
                    UserName = user.UserName,
                    Name = itsGenerator.GenerateName(),
                    Email = itsGenerator.GenerateEmail(),
                    Phone = itsGenerator.GeneratePhone(),
                    Address = itsGenerator.GenerateAddress(),
                    JobTitle = "Software Engineer"
                };
            IProfile actual = null;
            
            expected = itsTarget.Insert(expected);

            unitOfWork.Commit();
            unitOfWork = itsTarget.Provider.GetUnitOfWork();

            itsProfileIds.Add(expected.PrimaryId);

            expected.Email = itsGenerator.GenerateEmail();
            expected.JobTitle = "Software Development Manager";

            actual = itsTarget.Update(expected);

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.PrimaryId);
            Assert.AreNotEqual(Guid.Empty,actual.UserId);
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastModified,actual.LastModified);
            Assert.AreEqual(expected.Name,actual.Name);
            Assert.AreEqual(expected.Email,actual.Email);
            Assert.AreEqual(expected.Phone,actual.Phone);
            Assert.AreEqual(expected.Address,actual.Address);
            Assert.AreEqual(expected.JobTitle,actual.JobTitle);
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


        protected abstract IModule
        GetPersistenceModule();


    }
}

//  ##########################################################################
