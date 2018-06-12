//  ##########################################################################
//  # File Name: AuthenticationServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.RequiredService.User;
using NUnit.Framework;
using Strata.Foundation.Value;
using System;
using System.Collections.Generic;

namespace Granite.RequiredService.Authentication
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [Category("Integration")]
    [Ignore("not ready")]
    [TestFixture]
    public abstract
    class AuthenticationServiceTest
    {
        private IAuthenticationService          itsTarget;
        private IExternalUserService            itsUserService;
        private ISet<DeleteExternalUserRequest> itsDeletes;

        [SetUp]
        public virtual void
        SetUp()
        {
            itsTarget = CreateTarget();
            itsUserService = CreateUserService();
            itsDeletes = new HashSet<DeleteExternalUserRequest>();
        }

        [TearDown]
        public virtual void
        TearDown()
        {
            foreach (DeleteExternalUserRequest request in itsDeletes)
            {
                DeleteExternalUserReply reply = 
                    itsUserService.DeleteExternalUser(request);

                if (!reply.IsSuccessful)
                    Assert.Warn(
                        "Deleting {0} unsuccessful:\n{1}\n{2}",
                        reply.ExternalUserId,
                        reply.FailureMessage,
                        reply.StackTrace  );
            }
            
            itsUserService = null;
        }

        [TestCase("admin@ezvaluatoridentitymanager.com","yU44YkiB","Adam","Smith","adamsmith@yahoo.com","123.555.1111")]
        [TestCase("adamsmith@ezvaluatoridentitymanager.com","foo1bar@#$","Adam","Smith","adamsmith@yahoo.com","123.555.1111")]
        [TestCase("jeremy.bentham","biz1buz@#$","Jeremy","Bentham",null,"123.555.2222")]
        [TestCase("davidricardo@protonmail.com","foo!baz#1","David","Ricardo","davidricardo@protonmail.com","123.555.3333")]
        [TestCase("sjevons","foo!baz#2","Stanley","Jevons","stanly.jevons@gmail.com",null)]
        public void
        TestAuthenticate(
            string userName,
            string password,
            string firstName,
            string lastName,
            string email,
            string phone)
        {
            AuthenticateReply       reply = null;
            CreateExternalUserReply created =
                itsUserService.CreateExternalUser(
                    new CreateExternalUserRequest()
                    {
                        UserName = userName,
                        Password = password,
                        Name = new PersonName(firstName,lastName),
                        Email = string.IsNullOrEmpty(email) ? null : new EmailAddress(email),
                        Phone = string.IsNullOrEmpty(phone) ? null : new PhoneNumber(phone)
                    } );

            Assert.IsTrue(created.IsSuccessful);

            itsDeletes.Add(
                new DeleteExternalUserRequest()
                {
                    UserId = Guid.NewGuid(),
                    ExternalUserId = created.ExternalUserId
                });

            reply = 
                itsTarget.Authenticate(
                    new AuthenticateRequest()
                    {
                        UserName = userName,
                        Password = password
                    });

            Assert.NotNull(reply);
            Assert.NotNull(reply.AccessToken);
            Assert.NotNull(reply.AccessTokenType);
        }

        [TestCase("admin@ezvaluatoridentitymanager.onmicrosoft.com","yU44YkiB")]
        [TestCase("admin@ezvaluatoridentitymanager.onmicrosoft.com","yU44YkiB")]
        [Test]
        public void
        TestAuthenticateOnly(
            string userName,
            string password)
        {
            AuthenticateReply reply = 
                itsTarget.Authenticate(
                    new AuthenticateRequest()
                    {
                        UserName = userName,
                        Password = password
                    });

            Assert.NotNull(reply);
            Assert.NotNull(reply.AccessToken);
            Assert.NotNull(reply.AccessTokenType);
        }


        protected abstract IAuthenticationService
        CreateTarget();

        protected abstract IExternalUserService 
        CreateUserService();
    }
}

//  ##########################################################################
