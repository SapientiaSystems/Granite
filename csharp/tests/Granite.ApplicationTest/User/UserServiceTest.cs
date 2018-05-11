//  ##########################################################################
//  # File Name: UserServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Utility;
using Granite.ProvidedService.User;
using NUnit.Framework;
using Strata.Foundation.Injection;
using Strata.Foundation.Value;
using System;
using System.Collections.Generic;

namespace Granite.Application.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [Category("Integration")]
    [TestFixture]
    public abstract
    class UserServiceTest
    {
        private IUserService        itsTarget;
        private FakeEmailSender     itsSender;
        private ISet<RemoveRequest> itsRemovals;

        [SetUp]
        public virtual void
        SetUp()
        {
            IContainer container = CreateContainer();

            itsTarget = container.GetInstance<IUserService>();
            itsSender = (FakeEmailSender)container.GetInstance<IEmailSender>();
            itsRemovals = new HashSet<RemoveRequest>();
        }

        [TearDown]
        public virtual void
        TearDown()
        {
            foreach (RemoveRequest request in itsRemovals)
            {
                RemoveReply reply = itsTarget.Remove(request);

                if (!reply.IsSuccessful)
                    throw new Exception(reply.FailureMessage);
            }

            itsTarget = null;
        }

        static public object[] StartRegistrationTestCases =
        {
            new StartRegistrationRequest()
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
            new StartRegistrationRequest()
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
            new StartRegistrationRequest()
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
            new StartRegistrationRequest()
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
        [TestCaseSource("StartRegistrationTestCases")]
        public virtual void
        TestStartRegistration(StartRegistrationRequest request)
        {
            StartRegistrationReply reply =
                itsTarget.StartRegistration(request);
            string expectedMessage =
                string.Format(
                    "Registration for user '{0}' has started. " +
                    "A confirmation email was sent to '{1}'. " +
                    "Please following the instructions in this email to finish user registration. " +
                    "Thank you.",
                    request.UserName != null ? request.UserName : request.Email.ToString(),
                    request.Email.ToString());

            Assert.IsTrue(reply.IsSuccessful);

            itsRemovals.Add(
                new RemoveRequest()
                {
                    UserId = reply.UserId,
                    UserIdToRemove = reply.UserId
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

        [TestCaseSource("StartRegistrationTestCases")]
        public virtual void
        TestFinishRegistration(StartRegistrationRequest request)
        {
            FinishRegistrationReply reply = null;
            TestStartRegistration(request);

            Assert.NotNull(itsSender.Request);
            Assert.AreEqual(request.CorrelationId,itsSender.Request.CorrelationId);

            reply = itsTarget.FinishRegistration(itsSender.Request);
            Assert.AreEqual(itsSender.Request.CorrelationId,reply.CorrelationId);
            Assert.IsTrue(reply.IsSuccessful);
        }

        protected abstract IContainer 
        CreateContainer();
    }
}

//  ##########################################################################
