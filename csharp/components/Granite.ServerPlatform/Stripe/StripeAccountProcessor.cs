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
            CreateAccountData     account = request.AccountToCreate;
            CreateCreditCardData  cardData = account.CreditCardToCreate;
            PostalAddress         billing = cardData.BillingAddress;

            customer =
                customerService.Create(
                    new StripeCustomerCreateOptions()
                    {
                        Email       = account.Email,
                        Description = request.Description
                    });

            PushRollbackAction(
                () => { customerService.Delete(customer.Id); });

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
                () => { cardService.Delete(customer.Id,card.Id); });

            return
                new CreateExternalAccountReply()
                {
                    UserId = account.UserIdOfAccountToCreate,
                    ExternalAccountId = customer.Id,
                    Email = customer.Email,
                    ExternalPaymentMethodId = card.Id
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
            StripeCustomer snapshot = null;
            StripeDeleted deleted = null;

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
            throw new System.NotImplementedException();
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
            StripeCustomerService service = new StripeCustomerService();
            StripeCustomer        snapshot = null; 
            StripeCustomer        customer = null;

            snapshot = service.Get(request.ExternalAccountId);
            customer =
                service.Update(
                    request.ExternalAccountId,
                    ToCustomerUpdateOptions(request));

            PushRollbackAction(
                () => {
                    service.Update(
                        snapshot.Id,
                        new StripeCustomerUpdateOptions()
                        {
                            Description = snapshot.Description,
                            Email = snapshot.Email
                        });});

            reply.ExternalAccountId = customer.Id;
            reply.Email = customer.Email;
            reply.Description = customer.Description;
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
            StripeCardService    service  = new StripeCardService();
            StripeCard           snapshot = null;
            StripeCard           card     = null;
            CreateCreditCardData cardData = request.CreditCardToCreate;
            PostalAddress        billing  = cardData.BillingAddress;

            snapshot =
                service.Get(
                    request.ExternalAccountId,
                    request.ExternalPaymentMethodId);

            card =
                service.Create(
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

            service.Delete(request.ExternalAccountId,snapshot.Id);

            PushRollbackAction(
                () => { service.Delete(request.ExternalAccountId,card.Id); });

            reply.ExternalPaymentMethodId = card.Id;
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
