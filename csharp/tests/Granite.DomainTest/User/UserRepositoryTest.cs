//  ##########################################################################
//  # File Name: UserRepositoryTest.cs
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

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [TestFixture]
    public abstract
    class UserRepositoryTest
    {
        private IUserRepository    itsTarget;
        private ISet<Guid>         itsIds;
        private ITestUserGenerator itsGenerator;

        [SetUp]
        public virtual void
        SetUp()
        {
            AbstractModule.DefaultScope = typeof(ThreadScope);

            IContainer container = CreateContainer();

            itsTarget = container.GetInstance<IUserRepository>();
            itsIds = new HashSet<Guid>();
            itsGenerator = container.GetInstance<ITestUserGenerator>();
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

            IUser expected = itsGenerator.GenerateUser();
            IUser actual = itsTarget.Insert(expected);

            unitOfWork.Commit();

            itsIds.Add(actual.UserId);

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.UserId);
            Assert.AreEqual(expected.ExternalUserId,actual.ExternalUserId);
            Assert.AreEqual(expected.VerificationCode,actual.VerificationCode);
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

            IUser expected = itsGenerator.GenerateUser();
            IUser actual = itsTarget.Insert(expected);

            unitOfWork.Commit();
            unitOfWork = itsTarget.Provider.GetUnitOfWork();

            itsIds.Add(actual.UserId);

            expected = actual;
            expected.Email = itsGenerator.GenerateEmail();

            actual = itsTarget.Update(expected);
            unitOfWork.Commit();

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.UserId);
            Assert.AreEqual(expected.ExternalUserId,actual.ExternalUserId);
            Assert.AreEqual(expected.UserName,actual.UserName);
            Assert.AreEqual(expected.Email,actual.Email);
            Assert.AreEqual(expected.VerificationCode,actual.VerificationCode);
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

            IUser expected = itsGenerator.GenerateUser();
            IUser actual = null;

            expected = itsTarget.Insert(expected);

            unitOfWork.Commit();
            unitOfWork =
                itsTarget
                    .Provider
                    .GetUnitOfWork();

            itsIds.Add(expected.UserId);
            actual = itsTarget.GetUnique(expected.UserId);
            unitOfWork.Commit();

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.UserId);
            Assert.AreEqual(expected.UserName,actual.UserName);
            Assert.AreEqual(expected.Password,actual.Password);
            Assert.AreEqual(expected.ExternalUserId,actual.ExternalUserId);
            Assert.AreEqual(expected.VerificationCode,actual.VerificationCode);
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastModified,actual.LastModified);
            Assert.IsTrue(actual.Password.Matches(((StandardUser)expected).PlainTextPassword));
        }

        protected abstract IContainer
        CreateContainer();


    }
}

//  ##########################################################################
