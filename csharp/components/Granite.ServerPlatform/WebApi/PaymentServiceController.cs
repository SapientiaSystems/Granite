//  ##########################################################################
//  # File Name: PaymentServiceController.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Injection;
using Granite.ProvidedService.Payment;
using System.Web.Http;
using SystemEx.Injection;

namespace Granite.ServerPlatform.WebApi
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [RoutePrefix("v1/payment")]
    public
    class PaymentServiceController:
        ApiController,
        IPaymentService
    {
        private IPaymentService itsImp;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        PaymentServiceController()
        {
            itsImp = 
                Global
                    .Container
                    .GetInstance<IPaymentService>();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("create-plan")]
        public CreatePlanReply
        CreatePlan(CreatePlanRequest request)
        {
            return itsImp.CreatePlan(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("delete-plan")]
        public DeletePlanReply
        DeletePlan(DeletePlanRequest request)
        {
            return itsImp.DeletePlan(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("create-account")]
        public CreateAccountReply 
        CreateAccount(CreateAccountRequest request)
        {
            return itsImp.CreateAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("update-account")]
        public UpdateAccountReply 
        UpdateAccount(UpdateAccountRequest request)
        {
            return itsImp.UpdateAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("delete-account")]
        public DeleteAccountReply
        DeleteAccount(DeleteAccountRequest request)
        {
            return itsImp.DeleteAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("get-account")]
        public GetAccountReply 
        GetAccount(GetAccountRequest request)
        {
            return itsImp.GetAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("create-subscription")]
        public CreateSubscriptionReply 
        CreateSubscription(CreateSubscriptionRequest request)
        {
            return itsImp.CreateSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("update-subscription")]
        public UpdateSubscriptionReply 
        UpdateSubscription(UpdateSubscriptionRequest request)
        {
            return itsImp.UpdateSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("delete-subscription")]
        public DeleteSubscriptionReply
        DeleteSubscription(DeleteSubscriptionRequest request)
        {
            return itsImp.DeleteSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("get-subscription")]
        public GetSubscriptionReply 
        GetSubscription(GetSubscriptionRequest request)
        {
            return itsImp.GetSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("make-payment")]
        public MakePaymentReply 
        MakePayment(MakePaymentRequest request)
        {
            return itsImp.MakePayment(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("make-refund")]
        public MakeRefundReply 
        MakeRefund(MakeRefundRequest request)
        {
            return itsImp.MakeRefund(request);
        }
    }
}

//  ##########################################################################
