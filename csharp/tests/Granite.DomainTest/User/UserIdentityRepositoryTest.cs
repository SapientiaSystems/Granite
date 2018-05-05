//  ##########################################################################
//  # File Name: UserIdentityRepositoryTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using NUnit.Framework;
using Strata.Foundation.Injection;
using Strata.Ninject.Injection;
using Strata.Persistence.UnitOfWork;
using System;
using System.Collections.Generic;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [TestFixture]
    public abstract
    class UserIdentityRepositoryTest
    {
        private IUserRepository itsTarget;
        private ISet<Guid>              itsIds;

        [SetUp]
        public virtual void
        SetUp()
        {
            IContainer container = 
                new NinjectContainer(
                    GetPersistenceModule(),
                    new UserDomainModule());

            itsTarget = container.GetInstance<IUserRepository>();
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

            UserIdentity expected =
                new UserIdentity()
                {
                    ExternalUserId = "12345",
                    VerificationCode = "112233",
                    Status = UserStatus.PENDING,
                    Created = DateTime.Now,
                    LastModified = DateTime.Now
                };
            UserIdentity actual =
                itsTarget.Insert(expected);

            unitOfWork.Commit();

            itsIds.Add(actual.UserId);

            Assert.NotNull(actual);
            Assert.AreNotEqual(Guid.Empty,actual.UserId);
            Assert.AreEqual(expected.ExternalUserId,actual.ExternalUserId);
            Assert.AreEqual(expected.VerificationCode,actual.VerificationCode);
            Assert.AreEqual(expected.Created,actual.Created);
            Assert.AreEqual(expected.LastModified,actual.LastModified);
        }

        protected abstract IModule
        GetPersistenceModule();


    }
}

//  ##########################################################################
