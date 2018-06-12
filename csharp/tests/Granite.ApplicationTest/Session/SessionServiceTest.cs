//  ##########################################################################
//  # File Name: SessionServiceTest.cs
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
            IContainer           container = CreateContainer();
            IStartStopController starter   = container.GetInstance<IStartStopController>();

            starter.StartApplication();

            itsTarget = container.GetInstance<ISessionService>();
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

        protected abstract IContainer 
        CreateContainer();
    }
}

//  ##########################################################################
