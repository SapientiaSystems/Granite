//  ##########################################################################
//  # File Name: UserServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Verification;
using Granite.ProvidedService.Session;
using Granite.ProvidedService.User;
using NUnit.Framework;
using Strata.Foundation.Bootstrap;
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
        private IUserService            itsTarget;
        private FakeEmailSender         itsSender;
        private ISet<DeleteUserRequest> itsRemovals;
        private ISessionService         itsSessionService;
        private SessionData             itsSession;

        [SetUp]
        public virtual void
        SetUp()
        {
            IContainer           container = CreateContainer();
            IStartStopController starter = container.GetInstance<IStartStopController>();
            LoginReply           reply = null;

            starter.StartApplication();

            itsTarget = container.GetInstance<IUserService>();
            itsSessionService = container.GetInstance<ISessionService>();

            itsSender = (FakeEmailSender)container.GetInstance<IVerificationNotifier>();
            itsRemovals = new HashSet<DeleteUserRequest>();

            reply =
                itsSessionService.Login(
                    new LoginRequest()
                    {
                        CorrelationId = Guid.NewGuid(),
                        UserName = "test",
                        Password = "test"
                    });

            Assert.NotNull(reply);
            Assert.NotNull(reply.NewSession);

            itsSession = reply.NewSession;
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

            itsSessionService.Logout(
                new LogoutRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    SessionId = itsSession.SessionId,
                    AccessToken = itsSession.AccessToken
                });
            itsTarget = null;
        }

        public static object[] CreateUserTestCases =
        {
            new CreateUserRequest()
            {
                CorrelationId = Guid.NewGuid(),
                UserToCreate = 
                    new InitialUserData()
                    {
                        UserName = "adamsmith",
                        Password = "foobar1!@#",
                        Email = new EmailAddress("adamsmith@wealthofnations.com")
                    }
            },
            new CreateUserRequest()
            {
                CorrelationId = Guid.NewGuid(),
                UserToCreate = 
                    new InitialUserData()
                    {
                        UserName = "jeremy.bentham",
                        Password = "biz1buz@#$",
                        Email = new EmailAddress("jeremy@bentham.com")
                    }
            },
            new CreateUserRequest()
            {
                CorrelationId = Guid.NewGuid(),
                UserToCreate =
                    new InitialUserData()
                    {
                        UserName = "davidricardo@protonmail.com",
                        Password = "foo!baz#1",
                        Email = new EmailAddress("davidricardo@protonmail.com")
                    }
            },
            new CreateUserRequest()
            {
                CorrelationId = Guid.NewGuid(),
                UserToCreate =
                    new InitialUserData()
                    {
                        UserName = "sjevons",
                        Password = "foo!baz#2",
                        Email = new EmailAddress("stanly.jevons@gmail.com")
                    }
             }
        };
        [TestCaseSource("CreateUserTestCases")]
        public virtual void
        TestCreateUser(CreateUserRequest request)
        {
            CreateUserReply reply = null;
            UserData        newUser = null;
            string          expectedMessage = null;

            request.SessionId = itsSession.SessionId;
            request.AccessToken = itsSession.AccessToken;

            reply = itsTarget.CreateUser(request);
            Assert.NotNull(reply);
            Assert.IsTrue(reply.IsSuccessful);

            newUser = reply.NewUser;
            Assert.NotNull(newUser);

            expectedMessage =
                string.Format(
                    "Registration for user '{0}' has started. " +
                    "A confirmation email was sent to '{1}'. " +
                    "Please follow the instructions in this " +
                    "email to verify the user. Thank you.",
                    newUser.UserName != null ? newUser.UserName : newUser.Email.ToString(),
                    newUser.Email.ToString());

            itsRemovals.Add(
                new DeleteUserRequest()
                {
                    SessionId = itsSession.SessionId,
                    AccessToken = itsSession.AccessToken,
                    UserIdOfUserToDelete = newUser.UserId
                } );

            Assert.AreEqual(request.CorrelationId,reply.CorrelationId);

            if (string.IsNullOrEmpty(newUser.UserName))
                Assert.AreEqual(newUser.Email.ToString(),newUser.UserName);
            else
                Assert.AreEqual(newUser.UserName,newUser.UserName);

            if (newUser.Email != null)
                Assert.AreEqual(newUser.Email.ToString(),newUser.Email.ToString());

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

            itsSender.Request.SessionId = itsSession.SessionId;
            itsSender.Request.AccessToken = itsSession.AccessToken;

            reply = itsTarget.VerifyUser(itsSender.Request);
            Assert.AreEqual(itsSender.Request.CorrelationId,reply.CorrelationId);
            Assert.IsTrue(reply.IsSuccessful);
        }

        [Test]
        public virtual void
        TestGetUserCurrentUser()
        {
            GetUserReply reply =
                itsTarget.GetUser(
                    new GetUserRequest()
                    {
                        SessionId = itsSession.SessionId,
                        AccessToken = itsSession.AccessToken,
                        GetCurrentUser = true
                    });

            Assert.NotNull(reply);
            Assert.IsTrue(reply.IsSuccessful);
            Assert.NotNull(reply.RequestedUser);
            Assert.AreEqual("test",reply.RequestedUser.UserName);
            Assert.AreEqual(itsSession.UserId,reply.RequestedUser.UserId);
        }

        [Test]
        public virtual void
        TestGetUserUserName()
        {
            GetUserReply reply =
                itsTarget.GetUser(
                    new GetUserRequest()
                    {
                        SessionId = itsSession.SessionId,
                        AccessToken = itsSession.AccessToken,
                        GetCurrentUser = false,
                        UserNameOfUserToGet = "test"
                    });

            Assert.NotNull(reply);
            Assert.IsTrue(reply.IsSuccessful);
            Assert.NotNull(reply.RequestedUser);
            Assert.AreEqual("test",reply.RequestedUser.UserName);
            Assert.AreEqual(itsSession.UserId,reply.RequestedUser.UserId);
        }

        [Test]
        public virtual void
        TestGetUserUserId()
        {
            GetUserReply reply =
                itsTarget.GetUser(
                    new GetUserRequest()
                    {
                        SessionId = itsSession.SessionId,
                        AccessToken = itsSession.AccessToken,
                        GetCurrentUser = false,
                        UserIdOfUserToGet = itsSession.UserId
                    });

            Assert.NotNull(reply);
            Assert.IsTrue(reply.IsSuccessful);
            Assert.NotNull(reply.RequestedUser);
            Assert.AreEqual("test",reply.RequestedUser.UserName);
            Assert.AreEqual(itsSession.UserId,reply.RequestedUser.UserId);
        }

        protected abstract IContainer 
        CreateContainer();
    }
}

//  ##########################################################################
