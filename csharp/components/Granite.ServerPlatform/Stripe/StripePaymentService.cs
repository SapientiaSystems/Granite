//  ##########################################################################
//  # File Name: StripePaymentService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.RequiredService.Payment;
using SystemEx.Injection;

namespace Granite.ServerPlatform.Stripe
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class StripePaymentService:
        IExternalPaymentService
    {
        private IExternalPlanProcessor         itsPlanProcessor;
        private IExternalAccountProcessor      itsAccountProcessor;
        private IExternalSubscriptionProcessor itsSubscriptionProcessor;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        StripePaymentService(
            IStripeContext                 context,
            IExternalPlanProcessor         planProcessor,
            IExternalAccountProcessor      accountProcessor,
            IExternalSubscriptionProcessor subscriptionProcessor)
        {
            if (!context.IsAuthenticated)
                context.Authenticate();

            itsPlanProcessor         = planProcessor;
            itsAccountProcessor      = accountProcessor;
            itsSubscriptionProcessor = subscriptionProcessor;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public CreateExternalPlanReply
        CreateExternalPlan(CreateExternalPlanRequest request)
        {
            return itsPlanProcessor.CreateExternalPlan(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public DeleteExternalPlanReply
        DeleteExternalPlan(DeleteExternalPlanRequest request)
        {
            return itsPlanProcessor.DeleteExternalPlan(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public CreateExternalAccountReply
        CreateExternalAccount(CreateExternalAccountRequest request)
        {
            return itsAccountProcessor.CreateExternalAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public UpdateExternalAccountReply
        UpdateExternalAccount(UpdateExternalAccountRequest request)
        {
            return itsAccountProcessor.UpdateExternalAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public DeleteExternalAccountReply
        DeleteExternalAccount(DeleteExternalAccountRequest request)
        {
            return itsAccountProcessor.DeleteExternalAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public GetExternalAccountReply
        GetExternalAccount(GetExternalAccountRequest request)
        {
            return itsAccountProcessor.GetExternalAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public CreateExternalSubscriptionReply
        CreateExternalSubscription(CreateExternalSubscriptionRequest request)
        {
            return 
                itsSubscriptionProcessor.CreateExternalSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public UpdateExternalSubscriptionReply
        UpdateExternalSubscription(UpdateExternalSubscriptionRequest request)
        {
            return
                itsSubscriptionProcessor.UpdateExternalSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public DeleteExternalSubscriptionReply
        DeleteExternalSubscription(DeleteExternalSubscriptionRequest request)
        {
            return
                itsSubscriptionProcessor.DeleteExternalSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public MakeExternalPaymentReply
        MakeExternalPayment(MakeExternalPaymentRequest request)
        {
            return new MakeExternalPaymentReply();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public MakeExternalRefundReply
        MakeExternalRefund(MakeExternalRefundRequest request)
        {
            return new MakeExternalRefundReply();
        }

    }
}

//  ##########################################################################
