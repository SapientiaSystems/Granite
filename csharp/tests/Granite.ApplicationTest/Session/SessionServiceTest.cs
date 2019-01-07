//  ##########################################################################
//  # File Name: SessionServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Session;
using Ninject;
using NUnit.Framework;
using Strata.Foundation.Bootstrap;
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
    [TestFixture]
    public abstract
    class SessionServiceTest
    {
        private ISessionService     itsTarget;
        private ISet<LogoutRequest> itsLogouts;

        [SetUp]
        public virtual void
        SetUp()
        {
            IKernel              container = CreateContainer();
            IStartStopController starter   = container.Get<IStartStopController>();

            starter.StartApplication();

            itsTarget = container.Get<ISessionService>();
            itsLogouts = new HashSet<LogoutRequest>();
        }

        [TearDown]
        public virtual void
        TearDown()
        {
            foreach (LogoutRequest request in itsLogouts)
            {
                LogoutReply reply = itsTarget.Logout(request);

                if (!reply.IsSuccessful)
                    throw new Exception(reply.FailureMessage);
            }
            
            itsTarget = null;
        }

        [Test]
        public virtual void
        TestLoginGuestSuccess()
        {
            LoginRequest request =
                new LoginRequest()
                {
                    UserName = "guest",
                    Password = "guest"
                };
            LoginReply reply =
                itsTarget.Login(request);

            Assert.IsTrue(reply.IsSuccessful);

            itsLogouts.Add(
                new LogoutRequest()
                {
                    SessionId = reply.NewSession.SessionId,
                    AccessToken = reply.NewSession.AccessToken
                });

            Assert.NotNull(reply.NewSession);
        }

        [Test]
        public virtual void
        TestLoginGuestFailure()
        {
            LoginRequest request =
                new LoginRequest()
                {
                    UserName = "guest",
                    Password = "guest1"
                };
            LoginReply reply =
                itsTarget.Login(request);

            Assert.IsFalse(reply.IsSuccessful);
            Assert.IsNull(reply.NewSession);
        }

        [Test]
        public virtual void
        TestGetOrLoginValidAccessToken()
        {
            LoginRequest loginRequest =
                new LoginRequest()
                {
                    UserName = "test",
                    Password = "test"
                };
            LoginReply loginReply =
                itsTarget.Login(loginRequest);
            GetOrLoginRequest request =
                new GetOrLoginRequest()
                {
                    AccessToken = loginReply.NewSession.AccessToken,
                    UserName = "guest",
                    Password = "guest"
                };
            GetOrLoginReply reply =
                itsTarget.GetOrLogin(request);

            Assert.IsTrue(
                reply.IsSuccessful,
                reply.FailureMessage + "\n" + reply.StackTrace);
            Assert.NotNull(reply.Session);

            itsLogouts.Add(
                new LogoutRequest()
                {
                    SessionId = loginReply.NewSession.SessionId,
                    AccessToken = loginReply.NewSession.AccessToken
                });
        }

        [Test]
        public virtual void
        TestGetOrLoginInvalidAccessToken()
        {
            GetOrLoginRequest request =
                new GetOrLoginRequest()
                {
                    AccessToken = "Invalid-Token-XXXXXXXXXXXXXXX",
                    UserName = "guest",
                    Password = "guest"
                };
            GetOrLoginReply reply =
                itsTarget.GetOrLogin(request);

            Assert.IsTrue(
                reply.IsSuccessful,
                reply.FailureMessage + "\n" + reply.StackTrace);
            Assert.NotNull(reply.Session);

            itsLogouts.Add(
                new LogoutRequest()
                {
                    SessionId = reply.Session.SessionId,
                    AccessToken = reply.Session.AccessToken
                });
        }

        protected abstract IKernel 
        CreateContainer();
    }
}

//  ##########################################################################
