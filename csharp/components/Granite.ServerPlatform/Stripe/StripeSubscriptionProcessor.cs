//  ##########################################################################
//  # File Name: StripeSubscriptionProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Payment;
using Granite.RequiredService.Payment;
using Strata.Application.Service;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;
using Stripe;
using System;

namespace Granite.ServerPlatform.Stripe
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class StripeSubscriptionProcessor:
        AbstractService,
        IExternalSubscriptionProcessor
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        public
        StripeSubscriptionProcessor(
            IUnitOfWorkProvider provider,
            ILogger             logger):
            base(provider,logger) {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public CreateExternalSubscriptionReply 
        CreateExternalSubscription(CreateExternalSubscriptionRequest request)
        {
            StripeSubscriptionService service =
                new StripeSubscriptionService();
            StripeSubscription        subscription = null;
            CreateSubscriptionData    subscriptionData = 
                                          request.SubscriptionToCreate;

            subscription =
                service.Create(
                    request.ExternalAccountId,
                    new StripeSubscriptionCreateOptions()
                    {
                        CustomerId = request.ExternalAccountId,
                        PlanId = subscriptionData.SubscriptionType,
                        Billing = StripeBilling.ChargeAutomatically,
                        BillingCycleAnchorNow = true,

                    });

            PushRollbackAction(() => { service.Cancel(subscription.Id); });

            return 
                new CreateExternalSubscriptionReply()
                {
                    ExternalSubscriptionId = subscription.Id,
                    IsSuccessful = true,
                    SuccessMessage = "Subscription created."
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public UpdateExternalSubscriptionReply 
        UpdateExternalSubscription(UpdateExternalSubscriptionRequest request)
        {
            throw new NotImplementedException();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public DeleteExternalSubscriptionReply 
        DeleteExternalSubscription(DeleteExternalSubscriptionRequest request)
        {
            StripeSubscriptionService service =
                new StripeSubscriptionService();
            StripeSubscription cancelled = 
                service.Cancel(request.ExternalSubscriptionId);

            return
                new DeleteExternalSubscriptionReply()
                {
                    IsSuccessful = true,
                    SuccessMessage = "Subscription is deleted"
                };
        }
    }
}

//  ##########################################################################
