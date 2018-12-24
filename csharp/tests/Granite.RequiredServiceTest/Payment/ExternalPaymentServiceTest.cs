//  ##########################################################################
//  # File Name: ExternalPaymentServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Value;
using Granite.Domain.Shared;
using Granite.ProvidedService.Payment;
using Ninject;
using NUnit.Framework;
using Strata.Foundation.Injection;
using Strata.Foundation.Value;
using System;
using System.Collections.Generic;

namespace Granite.RequiredService.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [Category("Integration")]
    [TestFixture]
    public abstract
    class ExternalPaymentServiceTest
    {
        private IExternalPaymentService                 itsTarget;
        private ISet<DeleteExternalPlanRequest>         itsPlanDeletes;
        private ISet<DeleteExternalAccountRequest>      itsAccountDeletes;
        private ISet<DeleteExternalSubscriptionRequest> itsSubscriptionDeletes;
        private ITestUserGenerator                      itsGenerator;

        [SetUp]
        public virtual void
        SetUp()
        {
            IKernel container = CreateContainer();
            itsTarget = container.Get<IExternalPaymentService>();
            itsPlanDeletes = new HashSet<DeleteExternalPlanRequest>();
            itsAccountDeletes = new HashSet<DeleteExternalAccountRequest>();
            itsSubscriptionDeletes = new HashSet<DeleteExternalSubscriptionRequest>();
            itsGenerator = container.Get<ITestUserGenerator>();
        }

        [TearDown]
        public virtual void
        TearDown()
        {
            foreach (DeleteExternalSubscriptionRequest request in itsSubscriptionDeletes)
            {
                DeleteExternalSubscriptionReply reply =
                    itsTarget.DeleteExternalSubscription(request);

                if (!reply.IsSuccessful)
                    Assert.Warn(
                        "Deleting {0} unsuccessful:\n{1}\n{2}",
                        reply.ExternalSubscriptionId,
                        reply.FailureMessage,
                        reply.StackTrace);
            }

            foreach (DeleteExternalAccountRequest request in itsAccountDeletes)
            {
                DeleteExternalAccountReply reply =
                    itsTarget.DeleteExternalAccount(request);

                if (!reply.IsSuccessful)
                    Assert.Warn(
                        "Deleting {0} unsuccessful:\n{1}\n{2}",
                        reply.ExternalAccountId,
                        reply.FailureMessage,
                        reply.StackTrace);
            }

            foreach (DeleteExternalPlanRequest request in itsPlanDeletes)
            {
                DeleteExternalPlanReply reply = 
                    itsTarget.DeleteExternalPlan(request);

                if (!reply.IsSuccessful)
                    Assert.Warn(
                        "Deleting {0} unsuccessful:\n{1}\n{2}",
                        reply.ExternalPlanId,
                        reply.FailureMessage,
                        reply.StackTrace  );
            }
            
            itsTarget = null;
        }

        [TestCase("bronze-level","55.95")]
        [TestCase("silver-level","75.95")]
        [TestCase("gold-level","95.95")]
        [TestCase("platinum-level","125.95")]
        public void
        TestCreateExternalPlan(string planName,string amount)
        {
            CreateExternalPlanReply reply =
                itsTarget.CreateExternalPlan(
                    new CreateExternalPlanRequest()
                    {
                        PlanToCreate = 
                            new InitialPlanData()
                            {
                                PlanName = planName,
                                IntervalType = PaymentIntervalKind.MONTH,
                                IntervalCount = 1,
                                Currency = "USD",
                                Amount = Convert.ToDecimal(amount)
                            }
                       
                    } );

            Assert.IsTrue(reply.IsSuccessful);
            
            itsPlanDeletes.Add(
                new DeleteExternalPlanRequest()
                {
                    ExternalPlanId = reply.ExternalPlanId
                });
               
        }

        [TestCase("xyz@gmail.com","Adam Smith","4242424242424242",1,2020,"123")]
        public void
        TestCreateExternalAccount(string email,string name,string card,int month,int year,string code)
        {
            CreateExternalAccountReply reply =
                itsTarget.CreateExternalAccount(
                    new CreateExternalAccountRequest()
                    {
                        AccountToCreate = 
                            new InitialAccountData()
                            {
                                Description = "XYZ payment account",
                                Email = email,
                                UserIdOfAccountToCreate = Guid.NewGuid(),
                                CreditCardToCreate = 
                                    new InitialCreditCardData()
                                    {
                                        BillingAddress = itsGenerator.GenerateAddress(),
                                        CardHolderName = name,
                                        CardNumber = card,
                                        ExpirationMonth = month,
                                        ExpirationYear = year,
                                        SecurityCode = code
                                    }
                            }
                    });
            Assert.IsTrue(reply.IsSuccessful);

            itsAccountDeletes.Add(
                new DeleteExternalAccountRequest()
                {
                    ExternalAccountId = reply.ExternalAccountId
                });

        }

        [Test]
        public void
        TestCreateExternalSubscription()
        {
            CreateExternalPlanReply planReply =
                itsTarget.CreateExternalPlan(
                    new CreateExternalPlanRequest()
                    {
                        PlanToCreate =
                            new InitialPlanData()
                            {
                                PlanName = "Enterprise",
                                IntervalType = PaymentIntervalKind.MONTH,
                                IntervalCount = 1,
                                Currency = "USD",
                                Amount = 125.95m
                            }

                    });

            Assert.IsTrue(planReply.IsSuccessful);

            itsPlanDeletes.Add(
                new DeleteExternalPlanRequest()
                {
                    ExternalPlanId = planReply.ExternalPlanId
                });

            CreateExternalAccountReply accountReply =
                itsTarget.CreateExternalAccount(
                    new CreateExternalAccountRequest()
                    {
                        AccountToCreate =
                            new InitialAccountData()
                            {
                                Description = "XYZ payment account",
                                Email = "herbert.simon@email.com",
                                UserIdOfAccountToCreate = Guid.NewGuid(),
                                CreditCardToCreate =
                                    new InitialCreditCardData()
                                    {
                                        BillingAddress = itsGenerator.GenerateAddress(),
                                        CardHolderName = "Herbert Simon",
                                        CardNumber = "4242424242424242",
                                        ExpirationMonth = 3,
                                        ExpirationYear = 2020,
                                        SecurityCode = "235"
                                    }
                            }
                    });
            Assert.IsTrue(accountReply.IsSuccessful);

            itsAccountDeletes.Add(
                new DeleteExternalAccountRequest()
                {
                    ExternalAccountId = accountReply.ExternalAccountId
                });

            CreateExternalSubscriptionReply reply =
                itsTarget.CreateExternalSubscription(
                    new CreateExternalSubscriptionRequest()
                    {
                        ExternalAccountId = accountReply.ExternalAccountId,
                        SubscriptionToCreate = 
                            new InitialSubscriptionData()
                            {
                                SubscriptionType = planReply.ExternalPlanId
                            }
                    });

            Assert.IsTrue(reply.IsSuccessful);

            itsSubscriptionDeletes.Add(
                new DeleteExternalSubscriptionRequest()
                {
                    ExternalSubscriptionId = reply.ExternalSubscriptionId
                });
        }


        protected abstract IKernel 
        CreateContainer();
    }
}

//  ##########################################################################
