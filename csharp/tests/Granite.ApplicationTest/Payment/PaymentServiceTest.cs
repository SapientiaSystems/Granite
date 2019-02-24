//  ##########################################################################
//  # File Name: PaymentServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Verification;
using Granite.ProvidedService.Payment;
using Granite.ProvidedService.Session;
using Ninject;
using NUnit.Framework;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Value;
using System;
using System.Collections.Generic;

namespace Granite.Application.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [Category("Integration")]
    [TestFixture]
    public abstract
    class PaymentServiceTest
    {
        private IPaymentService                 itsTarget;
        private FakeEmailSender                 itsSender;
        private ISet<DeleteSubscriptionRequest> itsRemovals;
        private ISessionService                 itsSessionService;
        private SessionData                     itsSession;
        private AccountData                     itsAccount;

        [SetUp]
        public virtual void
        SetUp()
        {
            IKernel              container = CreateContainer();
            IStartStopController starter = container.Get<IStartStopController>();
            LoginReply           loginReply = null;
            CreateAccountReply   accountReply = null;

            starter.StartApplication();

            itsTarget = container.Get<IPaymentService>();
            itsSessionService = container.Get<ISessionService>();

            itsSender = (FakeEmailSender)container.Get<IVerificationNotifier>();
            itsRemovals = new HashSet<DeleteSubscriptionRequest>();

            loginReply =
                itsSessionService.Login(
                    new LoginRequest()
                    {
                        CorrelationId = Guid.NewGuid(),
                        UserName = "test",
                        Password = "test"
                    });

            Assert.NotNull(loginReply);
            Assert.IsTrue(loginReply.IsSuccessful,loginReply.FailureMessage);
            Assert.NotNull(loginReply.NewSession);

            itsSession = loginReply.NewSession;

            accountReply = DoTestCreateAccount();

            Assert.NotNull(accountReply);
            Assert.IsTrue(accountReply.IsSuccessful,accountReply.FailureMessage);
            Assert.NotNull(accountReply.NewAccount);

            itsAccount = accountReply.NewAccount;

        }

        [TearDown]
        public virtual void
        TearDown()
        {
            foreach (DeleteSubscriptionRequest request in itsRemovals)
            {
                DeleteSubscriptionReply reply = itsTarget.DeleteSubscription(request);

                if (!reply.IsSuccessful)
                    throw new Exception(reply.FailureMessage);
            }

            itsTarget.DeleteAccount(
                new DeleteAccountRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    SessionId = itsSession.SessionId,
                    AccessToken = itsSession.AccessToken,
                    AccountIdOfAccountToDelete = itsAccount.AccountId
                });
            itsSessionService.Logout(
                new LogoutRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    SessionId = itsSession.SessionId,
                    AccessToken = itsSession.AccessToken
                });
            itsTarget = null;
        }

        [Test]
        public virtual void
        TestCreateSubscription()
        {
            CreateSubscriptionRequest request = null;
            CreateSubscriptionReply   reply = null;
            SubscriptionData          newSubscription = null;

            request =
                new CreateSubscriptionRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    SessionId = itsSession.SessionId,
                    AccessToken = itsSession.AccessToken,
                    SubscriptionToCreate =
                        new InitialSubscriptionData()
                        {
                            UserId = itsSession.User.UserId,
                            AccountId = itsAccount.AccountId,
                            SubscriptionType = "Test"
                        }
                };
            request.SessionId = itsSession.SessionId;
            request.AccessToken = itsSession.AccessToken;

            reply = itsTarget.CreateSubscription(request);
            Assert.NotNull(reply);
            Assert.IsTrue(reply.IsSuccessful,reply.FailureMessage);

            newSubscription = reply.NewSubscription;
            Assert.NotNull(newSubscription);

            itsRemovals.Add(
                new DeleteSubscriptionRequest()
                {
                    SessionId = itsSession.SessionId,
                    AccessToken = itsSession.AccessToken,
                    SubscriptionIdOfSubscriptionToDelete = newSubscription.SubscriptionId
                } );

            Assert.AreEqual(request.CorrelationId,reply.CorrelationId);
        }

        [Test]
        public virtual void
        TestUpdateAccount()
        {
            UpdateAccountRequest request = null;
            UpdateAccountReply   reply = null;

            request =
                new UpdateAccountRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    SessionId = itsSession.SessionId,
                    AccessToken = itsSession.AccessToken,
                    AccountToUpdate =
                        new UpdateAccountData()
                        {
                            UserIdOfAccountToUpdate = itsSession.User.UserId,
                            ReplaceCreditCard = true,
                            CreditCardToCreate =
                                new InitialCreditCardData()
                                {
                                    CardHolderName =  "John Liebenau",
                                    BillingAddress =
                                        new PostalAddress()
                                        {
                                            StreetAddress = "12407 Creekwood Ave",
                                            City = "Cerritos",
                                            State =  "CA",
                                            PostalCode = "90703",
                                            CountryCode =  "US"
                                        },
                                    CardNumber =  "5555555555554444",
                                    ExpirationMonth = 10,
                                    ExpirationYear =  2020,
                                    SecurityCode =  "232"
                                }
                        }
                };

            reply = itsTarget.UpdateAccount(request);
            Assert.NotNull(reply);
            Assert.IsTrue(reply.IsSuccessful,reply.FailureMessage);

            itsAccount = reply.UpdatedAccount;
            Assert.NotNull(itsAccount);

        }


        [Test]
        public virtual void
        TestUpdateSubscription()
        {
            CreateSubscriptionRequest request = null;
            CreateSubscriptionReply reply = null;
            SubscriptionData newSubscription = null;
            UpdateSubscriptionRequest updateRequest = null;
            UpdateSubscriptionReply updateReply = null;

            request =
                new CreateSubscriptionRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    SessionId = itsSession.SessionId,
                    AccessToken = itsSession.AccessToken,
                    SubscriptionToCreate =
                        new InitialSubscriptionData()
                        {
                            UserId = itsSession.User.UserId,
                            AccountId = itsAccount.AccountId,
                            SubscriptionType = "Test"
                        }
                };
            request.SessionId = itsSession.SessionId;
            request.AccessToken = itsSession.AccessToken;

            reply = itsTarget.CreateSubscription(request);
            Assert.NotNull(reply);
            Assert.IsTrue(reply.IsSuccessful,reply.FailureMessage);

            newSubscription = reply.NewSubscription;
            Assert.NotNull(newSubscription);

            itsRemovals.Add(
                new DeleteSubscriptionRequest()
                {
                    SessionId = itsSession.SessionId,
                    AccessToken = itsSession.AccessToken,
                    SubscriptionIdOfSubscriptionToDelete = newSubscription.SubscriptionId
                });

            updateRequest =
                new UpdateSubscriptionRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    SessionId = itsSession.SessionId,
                    AccessToken = itsSession.AccessToken,
                    CurrentSubscription = newSubscription,
                    NewSubscription =
                        new InitialSubscriptionData()
                        {
                            UserId = itsSession.User.UserId,
                            AccountId = itsAccount.AccountId,
                            SubscriptionType = "Test"
                        }
                };
            Assert.AreEqual(request.CorrelationId,reply.CorrelationId);
        }

        [Test]
        public virtual void
        TestGetSubscriptionCurrentUser()
        {
            CreateSubscriptionRequest request = null;
            CreateSubscriptionReply reply = null;
            GetSubscriptionReply getReply = null;
            SubscriptionData newSubscription = null;

            request =
                new CreateSubscriptionRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    SessionId = itsSession.SessionId,
                    AccessToken = itsSession.AccessToken,
                    SubscriptionToCreate =
                        new InitialSubscriptionData()
                        {
                            UserId = itsSession.User.UserId,
                            AccountId = itsAccount.AccountId,
                            SubscriptionType = "Test"
                        }
                };
            request.SessionId = itsSession.SessionId;
            request.AccessToken = itsSession.AccessToken;

            reply = itsTarget.CreateSubscription(request);
            Assert.NotNull(reply);
            Assert.IsTrue(reply.IsSuccessful,reply.FailureMessage);

            newSubscription = reply.NewSubscription;
            Assert.NotNull(newSubscription);

            getReply =
                itsTarget.GetSubscription(
                    new GetSubscriptionRequest()
                    {
                        SessionId = itsSession.SessionId,
                        AccessToken = itsSession.AccessToken,
                        GetSubscriptionOfCurrentUser = true
                    });

            Assert.NotNull(getReply);
            Assert.IsTrue(getReply.IsSuccessful,getReply.FailureMessage);
            Assert.NotNull(getReply.RequestedSubscription);
        }

        protected CreateAccountReply 
        DoTestCreateAccount()
        {
            CreateAccountReply reply =
                itsTarget.CreateAccount(
                    new CreateAccountRequest()
                    {
                        SessionId = itsSession.SessionId,
                        AccessToken = itsSession.AccessToken,
                        CorrelationId = Guid.NewGuid(),
                        AccountToCreate = 
                            new InitialAccountData()
                            {
                                UserIdOfAccountToCreate = itsSession.User.UserId,
                                CreditCardToCreate = 
                                    new InitialCreditCardData()
                                    {
                                        CardHolderName = "Test Test",
                                        BillingAddress = 
                                            new PostalAddress()
                                            {
                                                StreetAddress = "123 Main St",
                                                City = "Los Angeles",
                                                State = "CA",
                                                PostalCode = "90017",
                                                CountryCode = "US"
                                            },
                                        CardNumber = "4242424242424242",
                                        ExpirationMonth = 3,
                                        ExpirationYear = 2025,
                                        SecurityCode = "123"
                                    },
                                Description = "Test Account",
                                Email = "test@test.com"
                            }
                    } );

            return reply;
        }

        protected abstract IKernel 
        CreateContainer();
    }
}

//  ##########################################################################
