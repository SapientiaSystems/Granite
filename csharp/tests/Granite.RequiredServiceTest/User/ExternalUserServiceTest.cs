//  ##########################################################################
//  # File Name: ExternalUserServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using NUnit.Framework;
using Strata.Foundation.Value;
using System;
using System.Collections.Generic;

namespace Granite.RequiredService.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [Category("Integration")]
    [TestFixture]
    public abstract
    class ExternalUserServiceTest
    {
        private IExternalUserService            itsTarget;
        private ISet<DeleteExternalUserRequest> itsDeletes;

        [SetUp]
        public virtual void
        SetUp()
        {
            itsTarget = CreateTarget();
            itsDeletes = new HashSet<DeleteExternalUserRequest>();
        }

        [TearDown]
        public virtual void
        TearDown()
        {
            foreach (DeleteExternalUserRequest request in itsDeletes)
            {
                DeleteExternalUserReply reply = 
                    itsTarget.DeleteExternalUser(request);

                if (!reply.IsSuccessful)
                    Assert.Warn(
                        "Deleting {0} unsuccessful:\n{1}\n{2}",
                        reply.ExternalUserId,
                        reply.FailureMessage,
                        reply.StackTrace  );
            }
            
            itsTarget = null;
        }

        [TestCase("adamsmith1","foo1bar@#$","Adam","Smith","adamsmith@yahoo.com","123.555.1111")]
        /*
        [TestCase("jeremy.bentham","biz1buz@#$","Jeremy","Bentham",null,"123.555.2222")]
        [TestCase(null,"foo!baz#1","David","Ricardo","davidricardo@protonmail.com","123.555.3333")]
        [TestCase("sjevons","foo!baz#2","Stanley","Jevons","stanly.jevons@gmail.com",null)]
        */
        public void
        TestCreateExternalUser(
            string userName,
            string password,
            string firstName,
            string lastName,
            string email,
            string phone)
        {
            CreateExternalUserReply reply =
                itsTarget.CreateExternalUser(
                    new CreateExternalUserRequest()
                    {
                        UserName = userName,
                        Password = password,
                        Name = new PersonName(firstName,lastName),
                        Email = string.IsNullOrEmpty(email) ? null : new EmailAddress(email),
                        Phone = string.IsNullOrEmpty(phone) ? null : new PhoneNumber(phone)
                    } );

            Assert.IsTrue(reply.IsSuccessful);
            /*
            itsDeletes.Add(
                new DeleteExternalUserRequest()
                {
                    UserId = Guid.NewGuid(),
                    ExternalUserId = reply.ExternalUserId
                });
                */
        }

        [TestCase("adamsmith","foo1bar@#$","Adam","Smith","adamsmith@yahoo.com","123.555.1111","foo2bar*%@","234.555.2222")]
        [TestCase("jeremy.bentham","biz1buz@#$","Jeremy","Bentham",null,"123.555.2222","foo3bar*%@",null)]
        [TestCase(null,"foo!baz#1","David","Ricardo","davidricardo@protonmail.com","123.555.3333",null,"234.555.3333")]
        [TestCase("sjevons","foo!baz#2","Stanley","Jevons","stanly.jevons@gmail.com",null,null,null)]
        public void
        TestUpdateExternalUser(
            string userName,
            string password,
            string firstName,
            string lastName,
            string email,
            string phone,
            string updatedPassword,
            string updatedPhone)
        {
            CreateExternalUserReply reply1 =
                itsTarget.CreateExternalUser(
                    new CreateExternalUserRequest()
                    {
                        UserName = userName,
                        Password = password,
                        Name = new PersonName(firstName,lastName),
                        Email = string.IsNullOrEmpty(email) ? null : new EmailAddress(email),
                        Phone = string.IsNullOrEmpty(phone) ? null : new PhoneNumber(phone)
                    });

            Assert.IsTrue(reply1.IsSuccessful);

            UpdateExternalUserReply reply2 =
                itsTarget.UpdateExternalUser(
                    new UpdateExternalUserRequest()
                    {
                        UserId = Guid.NewGuid(),
                        ExternalUserId = reply1.ExternalUserId,
                        Password = string.IsNullOrEmpty(updatedPassword) ? null : updatedPassword,
                        Phone = string.IsNullOrEmpty(updatedPhone) ? null : new PhoneNumber(updatedPhone)
                    });

            Assert.IsTrue(reply2.IsSuccessful);
            
            itsDeletes.Add(
                new DeleteExternalUserRequest()
                {
                    UserId = Guid.NewGuid(),
                    ExternalUserId = reply2.ExternalUserId
                });
                
        }

        protected abstract IExternalUserService 
        CreateTarget();
    }
}

//  ##########################################################################
