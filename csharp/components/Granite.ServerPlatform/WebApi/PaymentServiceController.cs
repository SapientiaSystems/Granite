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
        [HttpPost]
        public CreatePlanReply
        CreatePlan(CreatePlanRequest request)
        {
            return itsImp.CreatePlan(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("delete-plan")]
        [HttpDelete]
        public DeletePlanReply
        DeletePlan(DeletePlanRequest request)
        {
            return itsImp.DeletePlan(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("create-account")]
        [HttpPost]
        public CreateAccountReply 
        CreateAccount(CreateAccountRequest request)
        {
            return itsImp.CreateAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("update-account")]
        [HttpPut]
        public UpdateAccountReply 
        UpdateAccount(UpdateAccountRequest request)
        {
            return itsImp.UpdateAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("delete-account")]
        [HttpDelete]
        public DeleteAccountReply
        DeleteAccount(DeleteAccountRequest request)
        {
            return itsImp.DeleteAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("get-account")]
        [HttpPost]
        public GetAccountReply 
        GetAccount(GetAccountRequest request)
        {
            return itsImp.GetAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("create-subscription")]
        [HttpPost]
        public CreateSubscriptionReply 
        CreateSubscription(CreateSubscriptionRequest request)
        {
            return itsImp.CreateSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("update-subscription")]
        [HttpPut]
        public UpdateSubscriptionReply 
        UpdateSubscription(UpdateSubscriptionRequest request)
        {
            return itsImp.UpdateSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("delete-subscription")]
        [HttpDelete]
        public DeleteSubscriptionReply
        DeleteSubscription(DeleteSubscriptionRequest request)
        {
            return itsImp.DeleteSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("get-subscription")]
        [HttpPost]
        public GetSubscriptionReply 
        GetSubscription(GetSubscriptionRequest request)
        {
            return itsImp.GetSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("make-payment")]
        [HttpPost]
        public MakePaymentReply 
        MakePayment(MakePaymentRequest request)
        {
            return itsImp.MakePayment(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("make-refund")]
        [HttpPost]
        public MakeRefundReply 
        MakeRefund(MakeRefundRequest request)
        {
            return itsImp.MakeRefund(request);
        }
    }
}

//  ##########################################################################
