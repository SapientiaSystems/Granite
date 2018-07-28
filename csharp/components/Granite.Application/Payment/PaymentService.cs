//  ##########################################################################
//  # File Name: PaymentService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Payment;
using SystemEx.Injection;

namespace Granite.Application.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class PaymentService:
        IPaymentService
    {
        private IPaymentProcessor itsProcessor;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        PaymentService(IPaymentProcessor processor)
        {
            itsProcessor = processor;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public CreatePlanReply
        CreatePlan(CreatePlanRequest request)
        {
            return itsProcessor.CreatePlan(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public DeletePlanReply
        DeletePlan(DeletePlanRequest request)
        {
            return itsProcessor.DeletePlan(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public CreateAccountReply 
        CreateAccount(CreateAccountRequest request)
        {
            return itsProcessor.CreateAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public UpdateAccountReply 
        UpdateAccount(UpdateAccountRequest request)
        {
            return itsProcessor.UpdateAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public DeleteAccountReply
        DeleteAccount(DeleteAccountRequest request)
        {
            return itsProcessor.DeleteAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public GetAccountReply 
        GetAccount(GetAccountRequest request)
        {
            return itsProcessor.GetAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public CreateSubscriptionReply 
        CreateSubscription(CreateSubscriptionRequest request)
        {
            return itsProcessor.CreateSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public UpdateSubscriptionReply 
        UpdateSubscription(UpdateSubscriptionRequest request)
        {
            return itsProcessor.UpdateSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public DeleteSubscriptionReply
        DeleteSubscription(DeleteSubscriptionRequest request)
        {
            return itsProcessor.DeleteSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public GetSubscriptionReply 
        GetSubscription(GetSubscriptionRequest request)
        {
            return itsProcessor.GetSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public MakePaymentReply 
        MakePayment(MakePaymentRequest request)
        {
            return itsProcessor.MakePayment(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public MakeRefundReply 
        MakeRefund(MakeRefundRequest request)
        {
            return itsProcessor.MakeRefund(request);
        }
    }
}

//  ##########################################################################
