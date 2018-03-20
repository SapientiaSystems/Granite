//  ##########################################################################
//  # File Name: StripePaymentService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.RequiredService.Payment;
using Stripe;
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
        private StripeAccountHelper itsAccountHelper;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        StripePaymentService(IStripeContext context)
        {
            if (!context.IsAuthenticated)
                context.Authenticate();

            itsAccountHelper = new StripeAccountHelper();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public CreateExternalAccountReply
        CreateExternalAccount(CreateExternalAccountRequest request)
        {
            CreateExternalAccountReply reply =
                new CreateExternalAccountReply();

            return itsAccountHelper.CreateExternalAccount(reply,request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public UpdateExternalAccountReply
        UpdateExternalAccount(UpdateExternalAccountRequest request)
        {
            UpdateExternalAccountReply reply = 
                new UpdateExternalAccountReply();

            if (itsAccountHelper.MustUpdateCustomer(request))
                reply = itsAccountHelper.UpdateCustomer(reply,request);

            if (itsAccountHelper.MustUpdateCard(request))
                reply = itsAccountHelper.UpdateCard(reply,request);

            return reply;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public DeleteExternalAccountReply
        DeleteExternalAccount(DeleteExternalAccountRequest request)
        {
            return new DeleteExternalAccountReply();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public CreateExternalSubscriptionReply
        CreateExternalSubscription(CreateExternalSubscriptionRequest request)
        {
            return new CreateExternalSubscriptionReply();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public UpdateExternalSubscriptionReply
        UpdateExternalSubscription(UpdateExternalSubscriptionRequest request)
        {
            return new UpdateExternalSubscriptionReply();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public DeleteExternalSubscriptionReply
        DeleteExternalSubscription(DeleteExternalSubscriptionRequest request)
        {
            return new DeleteExternalSubscriptionReply();
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
