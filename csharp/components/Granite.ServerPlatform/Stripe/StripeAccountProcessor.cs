//  ##########################################################################
//  # File Name: StripeCustomerHelper.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Payment;
using Granite.RequiredService.Payment;
using Strata.Application.Service;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;
using Strata.Foundation.Value;
using Stripe;
using SystemEx.Injection;

namespace Granite.ServerPlatform.Stripe
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class StripeAccountProcessor:
        AbstractService,
        IExternalAccountProcessor
    {

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        StripeAccountProcessor(
            IUnitOfWorkProvider provider,
            ILogger             logger): 
            base(provider,logger) {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public CreateExternalAccountReply
        CreateExternalAccount(CreateExternalAccountRequest request)
        {
            StripeCustomerService customerService =
                                      new StripeCustomerService();
            StripeCardService     cardService =
                                      new StripeCardService();
            StripeCustomer        customer = null;
            StripeCard            card = null;
            InitialAccountData    account = request.AccountToCreate;
            InitialCreditCardData cardData = account.CreditCardToCreate;
            PostalAddress         billing = cardData.BillingAddress;

            customer =
                customerService.Create(
                    new StripeCustomerCreateOptions()
                    {
                        Email       = account.Email,
                        Description = request.Description
                    });

            card =
                cardService.Create(
                    customer.Id,
                    new StripeCardCreateOptions()
                    {
                        SourceCard = 
                            new SourceCard()
                            {
                                Number = cardData.CardNumber,
                                ExpirationYear = cardData.ExpirationYear,
                                ExpirationMonth = cardData.ExpirationMonth,
                                Cvc = cardData.SecurityCode,
                                Name = cardData.CardHolderName,
                                AddressLine1 = billing.StreetAddress,
                                AddressCity = billing.City,
                                AddressState = billing.State,
                                AddressZip = billing.PostalCode,
                                AddressCountry = billing.CountryCode                               
                            }
                    });

            PushRollbackAction(
                () => 
                {
                    cardService.Delete(customer.Id,card.Id);
                    customerService.Delete(customer.Id);
                });

            return
                new CreateExternalAccountReply()
                {
                    UserId = account.UserIdOfAccountToCreate,
                    ExternalAccountId = customer.Id,
                    Email = customer.Email,
                    ExternalPaymentMethodId = card.Id,
                    CreditCard = 
                        new CreditCardData()
                        {
                            CardHolderName = card.Name,
                            BillingAddress = 
                                new PostalAddress()
                                {
                                    StreetAddress = card.AddressLine1,
                                    City = card.AddressCity,
                                    State = card.AddressState,
                                    PostalCode = card.AddressZip,
                                    CountryCode = card.AddressCountry
                                },
                            CardType = card.Brand,
                            ObfuscatedCardNumber = "************" + card.Last4,
                            ExpirationMonth = card.ExpirationMonth,
                            ExpirationYear = card.ExpirationYear
                        }
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public UpdateExternalAccountReply
        UpdateExternalAccount(UpdateExternalAccountRequest request)
        {
            UpdateExternalAccountReply reply =
                new UpdateExternalAccountReply();

            if (MustUpdateCustomer(request))
                UpdateCustomer(reply,request);

            if (request.ReplaceCreditCard)
                if (MustReplaceCard(request))
                    ReplaceCard(reply,request);
            else
                if (MustUpdateCard(request))
                    UpdateCard(reply,request);

            return reply;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public DeleteExternalAccountReply 
        DeleteExternalAccount(DeleteExternalAccountRequest request)
        {
            StripeCustomerService service = new StripeCustomerService();
            StripeCustomer        snapshot = null;
            StripeDeleted         deleted = null;

            snapshot = service.Get(request.ExternalAccountId);
            deleted = service.Delete(request.ExternalAccountId);

            if (!deleted.Deleted)
                return
                    new DeleteExternalAccountReply()
                    {
                        IsSuccessful = false,
                        FailureMessage = "Could not delete plan."
                    };

            return
                new DeleteExternalAccountReply()
                {
                    IsSuccessful = true,
                    SuccessMessage = "Deleted plan."
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public GetExternalAccountReply 
        GetExternalAccount(GetExternalAccountRequest request)
        {
            StripeCustomerService customerService =
                                      new StripeCustomerService();
            StripeCardService     cardService =
                                      new StripeCardService();
            StripeCustomer        customer = null;
            StripeCard            card = null;

            customer = customerService.Get(request.ExternalAccountId);
            card =
                cardService.Get(
                    request.ExternalAccountId,
                    request.ExternalPaymentMethodId);

            return
                new GetExternalAccountReply()
                {
                    ExternalAccountId = customer.Id,
                    Email = customer.Email,
                    CreditCard =
                        new CreditCardData()
                        {
                            CardHolderName = card.Name,
                            BillingAddress =
                                new PostalAddress()
                                {
                                    StreetAddress = card.AddressLine1,
                                    City = card.AddressCity,
                                    State = card.AddressState,
                                    PostalCode = card.AddressZip,
                                    CountryCode = card.AddressCountry
                                },
                            CardType = card.Brand,
                            ObfuscatedCardNumber = "************" + card.Last4,
                            ExpirationMonth = card.ExpirationMonth,
                            ExpirationYear = card.ExpirationYear
                        }
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        private bool
        MustUpdateCustomer(UpdateExternalAccountRequest request)
        {
            return request.HasEmail() || request.HasDescription();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        private UpdateExternalAccountReply
        UpdateCustomer(
            UpdateExternalAccountReply   reply,
            UpdateExternalAccountRequest request)
        {
            StripeCustomerService customerService = 
                                      new StripeCustomerService();
            StripeCardService     cardService =
                                      new StripeCardService();
            StripeCustomer        snapshot = null; 
            StripeCustomer        customer = null;
            StripeCard            card = null;

            snapshot = customerService.Get(request.ExternalAccountId);
            card = 
                cardService.Get(
                    request.ExternalAccountId,
                    request.ExternalPaymentMethodId);
            customer =
                customerService.Update(
                    request.ExternalAccountId,
                    ToCustomerUpdateOptions(request));

            PushRollbackAction(
                () => {
                    customerService.Update(
                        snapshot.Id,
                        new StripeCustomerUpdateOptions()
                        {
                            Description = snapshot.Description,
                            Email = snapshot.Email
                        });});

            reply.ExternalAccountId = customer.Id;
            reply.Email = customer.Email;
            reply.Description = customer.Description;
            reply.CreditCard =
                new CreditCardData()
                {
                    CardHolderName = card.Name,
                    BillingAddress =
                        new PostalAddress()
                        {
                            StreetAddress = card.AddressLine1,
                            City = card.AddressCity,
                            State = card.AddressState,
                            PostalCode = card.AddressZip,
                            CountryCode = card.AddressCountry
                        },
                    CardType = card.Brand,
                    ObfuscatedCardNumber = "************" + card.Last4,
                    ExpirationMonth = card.ExpirationMonth,
                    ExpirationYear = card.ExpirationYear
                };
            return reply;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        private StripeCustomerUpdateOptions
        ToCustomerUpdateOptions(UpdateExternalAccountRequest request)
        {
            StripeCustomerUpdateOptions update =
                new StripeCustomerUpdateOptions();

            if (request.HasEmail())
                update.Email = request.Email;

            if (request.HasDescription())
                update.Description = request.Description;

            return update;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        private bool
        MustReplaceCard(UpdateExternalAccountRequest request)
        {
            return
                request.HasExternalPaymentMethodId() &&
                request.ReplaceCreditCard &&
                request.CreditCardToCreate != null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        private UpdateExternalAccountReply
        ReplaceCard(
            UpdateExternalAccountReply   reply,
            UpdateExternalAccountRequest request)
        {
            StripeCardService     cardService  = new StripeCardService();
            StripeCustomerService customerService = new StripeCustomerService();
            StripeCard            snapshot = null;
            StripeCard            card     = null;
            StripeCustomer        customer = null;
            InitialCreditCardData cardData = request.CreditCardToCreate;
            PostalAddress         billing  = cardData.BillingAddress;

            snapshot =
                cardService.Get(
                    request.ExternalAccountId,
                    request.ExternalPaymentMethodId);

            card =
                cardService.Create(
                    request.ExternalAccountId,
                    new StripeCardCreateOptions()
                    {
                        SourceCard =
                            new SourceCard()
                            {
                                Number = cardData.CardNumber,
                                ExpirationYear = cardData.ExpirationYear,
                                ExpirationMonth = cardData.ExpirationMonth,
                                Cvc = cardData.SecurityCode,
                                Name = cardData.CardHolderName,
                                AddressLine1 = billing.StreetAddress,
                                AddressCity = billing.City,
                                AddressState = billing.State,
                                AddressZip = billing.PostalCode,
                                AddressCountry = billing.CountryCode
                            }
                    });

            customer = 
                customerService.Update(
                    request.ExternalAccountId,
                    new StripeCustomerUpdateOptions()
                    {
                        DefaultSource = card.Id
                    });

            PushRollbackAction(
                () => 
                {
                    customerService.Update(
                        customer.Id,
                        new StripeCustomerUpdateOptions()
                        {
                            DefaultSource = snapshot.Id
                        });
                    cardService.Delete(request.ExternalAccountId,card.Id);
                });


            reply.ExternalPaymentMethodId = card.Id;
            reply.CreditCard =
                new CreditCardData()
                {
                    CardHolderName = card.Name,
                    BillingAddress =
                        new PostalAddress()
                        {
                            StreetAddress = card.AddressLine1,
                            City = card.AddressCity,
                            State = card.AddressState,
                            PostalCode = card.AddressZip,
                            CountryCode = card.AddressCountry
                        },
                    CardType = card.Brand,
                    ObfuscatedCardNumber = "************" + card.Last4,
                    ExpirationMonth = card.ExpirationMonth,
                    ExpirationYear = card.ExpirationYear
                };
            return reply;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        private bool
        MustUpdateCard(UpdateExternalAccountRequest request)
        {
            return
                request.HasExternalPaymentMethodId() &&
                !request.ReplaceCreditCard &&
                request.CreditCardToCreate != null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        private UpdateExternalAccountReply
        UpdateCard(
            UpdateExternalAccountReply reply,
            UpdateExternalAccountRequest request)
        {
            StripeCardService service = new StripeCardService();
            StripeCard        snapshot = null;
            StripeCard        card = null;

            snapshot =
                service.Get(
                    request.ExternalAccountId,
                    request.ExternalPaymentMethodId);
            card =
                service.Update(
                    request.ExternalAccountId,
                    request.ExternalPaymentMethodId,
                    ToCardUpdateOptions(request));

            PushRollbackAction(
                () => {
                    service.Update(
                        request.ExternalAccountId,
                        snapshot.Id,
                        new StripeCardUpdateOptions()
                        {
                            ExpirationYear = snapshot.ExpirationYear,
                            ExpirationMonth = snapshot.ExpirationMonth,
                            Name = snapshot.Name,
                            AddressLine1 = snapshot.AddressLine1,
                            AddressCity = snapshot.AddressCity,
                            AddressState = snapshot.AddressState,
                            AddressZip = snapshot.AddressZip,
                            AddressCountry = snapshot.AddressCountry
                        });});

            reply.ExternalPaymentMethodId = card.Id;
            reply.CreditCard =
                new CreditCardData()
                {
                    CardHolderName = card.Name,
                    BillingAddress =
                        new PostalAddress()
                        {
                            StreetAddress = card.AddressLine1,
                            City = card.AddressCity,
                            State = card.AddressState,
                            PostalCode = card.AddressZip,
                            CountryCode = card.AddressCountry
                        },
                    CardType = card.Brand,
                    ObfuscatedCardNumber = "************" + card.Last4,
                    ExpirationMonth = card.ExpirationMonth,
                    ExpirationYear = card.ExpirationYear
                };
            return reply;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        private StripeCardUpdateOptions
        ToCardUpdateOptions(UpdateExternalAccountRequest request)
        {
            UpdateCreditCardData cardData = request.CreditCardToUpdate;
            PostalAddress        billing  = cardData.BillingAddress;

            return
                new StripeCardUpdateOptions()
                {
                    ExpirationYear = cardData.ExpirationYear,
                    ExpirationMonth = cardData.ExpirationMonth,
                    Name = cardData.CardHolderName,
                    AddressLine1 = billing.StreetAddress,
                    AddressCity = billing.City,
                    AddressState = billing.State,
                    AddressZip = billing.PostalCode,
                    AddressCountry = billing.CountryCode
                };
        }
    }
}

//  ##########################################################################
