//  ##########################################################################
//  # File Name: SessionServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Verification;
using Granite.ProvidedService.User;
using NUnit.Framework;
using Strata.Foundation.Injection;
using Strata.Foundation.Value;
using System;
using System.Collections.Generic;

namespace Granite.Application.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [Category("Integration")]
    [Ignore("not ready yet")]
    [TestFixture]
    public abstract
    class SessionServiceTest
    {
        private IUserService        itsTarget;
        private FakeEmailSender     itsSender;
        private ISet<DeleteUserRequest> itsRemovals;

        [SetUp]
        public virtual void
        SetUp()
        {
            IContainer container = CreateContainer();

            itsTarget = container.GetInstance<IUserService>();
            itsSender = (FakeEmailSender)container.GetInstance<IVerificationNotifier>();
            itsRemovals = new HashSet<DeleteUserRequest>();
        }

        [TearDown]
        public virtual void
        TearDown()
        {
            foreach (DeleteUserRequest request in itsRemovals)
            {
                DeleteUserReply reply = itsTarget.DeleteUser(request);

                if (!reply.IsSuccessful)
                    throw new Exception(reply.FailureMessage);
            }
            
            itsTarget = null;
        }

        static public object[] CreateUserTestCases =
        {
            new CreateUserRequest()
            {
                CorrelationId = Guid.NewGuid(),
                UserName = "adamsmith",
                Password = "foobar1!@#",
                Email = new EmailAddress("adamsmith@wealthofnations.com"),
                Name = new PersonName("Adam","Smith"),
                JobTitle = "Professor, Moral Philosphy",
                Address = new PostalAddress()
                    {
                        StreetAddress = "123 Main St.",
                        City = "Los Angeles",
                        State = "CA",
                        PostalCode = "90101",
                        CountryCode = "USA"
                    },
                Phone = new PhoneNumber("310.555.1234")
            },
            new CreateUserRequest()
            {
                CorrelationId = Guid.NewGuid(),
                UserName = "jeremy.bentham",
                Password = "biz1buz@#$",
                Email = new EmailAddress("jeremy@bentham.com"),
                Name = new PersonName("Jeremy","Bentham"),
                JobTitle = "Political Economist",
                Address = new PostalAddress()
                    {
                        StreetAddress = "2222 5th Ave.",
                        City = "San Diego",
                        State = "CA",
                        PostalCode = "94001",
                            CountryCode = "USA"
                    },
                Phone = new PhoneNumber("310.555.1234")
            },
            new CreateUserRequest()
            {
                CorrelationId = Guid.NewGuid(),
                UserName = null,
                Password = "foo!baz#1",
                Email = new EmailAddress("davidricardo@protonmail.com"),
                Name = new PersonName("David","Ricardo"),
                JobTitle = "Political Economist",
                Address = null,
                Phone = new PhoneNumber("213-555-2222")
            },
            new CreateUserRequest()
            {
                CorrelationId = Guid.NewGuid(),
                UserName = "sjevons",
                Password = "foo!baz#2",
                Email = new EmailAddress("stanly.jevons@gmail.com"),
                Name = null,
                JobTitle = "Economist",
                Address = new PostalAddress()
                    {
                        StreetAddress = "2357 Grand Ave.",
                        City = "Los Angeles",
                        State = "CA",
                        PostalCode = "90101",
                        CountryCode = "USA"
                    },
                Phone = null
            }
        };
        [TestCaseSource("CreateUserTestCases")]
        public virtual void
        TestCreateUser(CreateUserRequest request)
        {
            CreateUserReply reply =
                itsTarget.CreateUser(request);
            string expectedMessage =
                string.Format(
                    "Registration for user '{0}' has started. " +
                    "A confirmation email was sent to '{1}'. " +
                    "Please follow the instructions in this " +
                    "email to verify the user. Thank you.",
                    request.UserName != null ? request.UserName : request.Email.ToString(),
                    request.Email.ToString());

            Assert.IsTrue(reply.IsSuccessful);

            itsRemovals.Add(
                new DeleteUserRequest()
                {
                    UserId = reply.UserId,
                    UserIdToDelete = reply.UserId
                } );

            Assert.AreEqual(request.CorrelationId,reply.CorrelationId);

            if (string.IsNullOrEmpty(request.UserName))
                Assert.AreEqual(request.Email.ToString(),reply.UserName);
            else
                Assert.AreEqual(request.UserName,reply.UserName);

            Assert.AreEqual(request.JobTitle,reply.JobTitle);

            if (request.Email != null)
                Assert.AreEqual(request.Email.ToString(),reply.Email.ToString());

            Assert.AreEqual(expectedMessage,reply.SuccessMessage);
        }

        [TestCaseSource("CreateUserTestCases")]
        public virtual void
        TestVerifyUser(CreateUserRequest request)
        {
            VerifyUserReply reply = null;
            TestCreateUser(request);

            Assert.NotNull(itsSender.Request);
            Assert.AreEqual(request.CorrelationId,itsSender.Request.CorrelationId);

            reply = itsTarget.VerifyUser(itsSender.Request);
            Assert.AreEqual(itsSender.Request.CorrelationId,reply.CorrelationId);
            Assert.IsTrue(reply.IsSuccessful);
        }

        protected abstract IContainer 
        CreateContainer();
    }
}

//  ##########################################################################
