//  ##########################################################################
//  # File Name: StripeCustomerHelper.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.RequiredService.Payment;
using Stripe;
using System;

namespace Granite.ServerPlatform.Stripe
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    internal
    class StripeAccountHelper
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        internal CreateExternalAccountReply
        CreateExternalAccount(
            CreateExternalAccountReply   reply,
            CreateExternalAccountRequest request)
        {
            StripeCustomerService customerService =
                                      new StripeCustomerService();
            StripeSourceService   sourceService =
                                      new StripeSourceService();
            StripeTokenService    tokenService =
                                      new StripeTokenService();
            StripeCustomer        customer = null;
            StripeToken           token = null;

            customer =
                customerService.Create(
                    new StripeCustomerCreateOptions()
                    {
                        Email = request.Email,
                        Description = request.Description
                    });

            token =
                tokenService.Create(
                    new StripeTokenCreateOptions()
                    {
                        CustomerId = customer.Id,
                        Card =
                            new StripeCreditCardOptions()
                            {
                                Number = request.CreditCardNumber,
                                ExpirationYear = request.ExpirationYear,
                                ExpirationMonth = request.ExpirationMonth,
                                Cvc = request.SecurityCode
                            }
                    });


            reply.AccountId = customer.Id;
            reply.Email = customer.Email;
            reply.PaymentMethodId = token.Id;

            return reply;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        internal bool
        MustUpdateCustomer(UpdateExternalAccountRequest request)
        {
            return request.HasEmail() || request.HasDescription();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        internal UpdateExternalAccountReply
        UpdateCustomer(
            UpdateExternalAccountReply reply,
            UpdateExternalAccountRequest request)
        {
            StripeCustomerService service = new StripeCustomerService();
            StripeCustomer customer = null;

            customer =
                service.Update(
                    request.AccountId,
                    ToCustomerUpdateOptions(request));

            reply.AccountId = customer.Id;
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
        internal bool
        MustUpdateCard(UpdateExternalAccountRequest request)
        {
            return
                request.HasPaymentMethodId() ||
                request.HasCreditCardNumber() ||
                request.HasExpirationYear() ||
                request.HasExpirationMonth() ||
                request.HasSecurityCode();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        internal UpdateExternalAccountReply
        UpdateCard(
            UpdateExternalAccountReply reply,
            UpdateExternalAccountRequest request)
        {
            StripeCardService cardService = new StripeCardService();
            StripeTokenService tokenService = new StripeTokenService();
            StripeDeleted deletedCard = null;
            StripeToken token = null;

            deletedCard =
                cardService.Delete(
                    request.AccountId,
                    request.PaymentMethodId);

            token = tokenService.Create(ToTokenCreateOptions(request));

            reply.PaymentMethodId = token.Id;
            return reply;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        private StripeTokenCreateOptions
        ToTokenCreateOptions(UpdateExternalAccountRequest request)
        {
            return
                new StripeTokenCreateOptions()
                {
                    CustomerId = request.AccountId,
                    Card =
                        new StripeCreditCardOptions()
                        {
                            Number = request.CreditCardNumber,
                            ExpirationYear = request.ExpirationYear,
                            ExpirationMonth = request.ExpirationMonth,
                            Cvc = request.SecurityCode
                        }
                };
        }

    }
}

//  ##########################################################################
