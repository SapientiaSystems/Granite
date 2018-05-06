//  ##########################################################################
//  # File Name: PaymentServiceController.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Payment;
using System.Web.Http;
using System.Web.Http.Cors;
using System.Web.Mvc;
using SystemEx.Injection;

namespace Granite.ServerPlatform.WebApi
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [System.Web.Http.RoutePrefix("v1/payment")]
    [RequireHttps]
    [EnableCors("*","*","*")]
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
        PaymentServiceController(IPaymentService imp)
        {
            itsImp = imp;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("create-account")]
        [RequireHttps]
        public CreateAccountReply 
        CreateAccount(CreateAccountRequest request)
        {
            return itsImp.CreateAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("update-account")]
        [RequireHttps]
        public UpdateAccountReply 
        UpdateAccount(UpdateAccountRequest request)
        {
            return itsImp.UpdateAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("delete-account")]
        [RequireHttps]
        public DeleteAccountReply 
        DeleteAccount(DeleteAccountRequest request)
        {
            return itsImp.DeleteAccount(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("create-subscription")]
        [RequireHttps]
        public CreateSubscriptionReply 
        CreateSubscription(CreateSubscriptionRequest request)
        {
            return itsImp.CreateSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("update-subscription")]
        [RequireHttps]
        public UpdateSubscriptionReply 
        UpdateSubscription(UpdateSubscriptionRequest request)
        {
            return itsImp.UpdateSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("delete-subscription")]
        [RequireHttps]
        public DeleteSubscriptionReply 
        DeleteSubscription(DeleteSubscriptionRequest request)
        {
            return itsImp.DeleteSubscription(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("make-payment")]
        [RequireHttps]
        public MakePaymentReply 
        MakePayment(MakePaymentRequest request)
        {
            return itsImp.MakePayment(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("make-refund")]
        [RequireHttps]
        public MakeRefundReply 
        MakeRefund(MakeRefundRequest request)
        {
            return itsImp.MakeRefund(request);
        }
    }
}

//  ##########################################################################
