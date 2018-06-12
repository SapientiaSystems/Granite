//  ##########################################################################
//  # File Name: PaymentService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Payment;
using Strata.Application.Service;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;
using SystemEx.Injection;

namespace Granite.Application.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class PaymentService:
        AbstractService,
        IPaymentService
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        PaymentService(IUnitOfWorkProvider provider,ILogger logger) :
            base(provider,logger) {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public CreateAccountReply 
        CreateAccount(CreateAccountRequest request)
        {
            return new CreateAccountReply();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public UpdateAccountReply 
        UpdateAccount(UpdateAccountRequest request)
        {
            return new UpdateAccountReply();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public DeleteAccountReply 
        DeleteAccount(DeleteAccountRequest request)
        {
            return new DeleteAccountReply();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public CreateSubscriptionReply 
        CreateSubscription(CreateSubscriptionRequest request)
        {
            return new CreateSubscriptionReply();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public UpdateSubscriptionReply 
        UpdateSubscription(UpdateSubscriptionRequest request)
        {
            return new UpdateSubscriptionReply();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public DeleteSubscriptionReply 
        DeleteSubscription(DeleteSubscriptionRequest request)
        {
            return new DeleteSubscriptionReply();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public MakePaymentReply 
        MakePayment(MakePaymentRequest request)
        {
            return new MakePaymentReply();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public MakeRefundReply 
        MakeRefund(MakeRefundRequest request)
        {
            return new MakeRefundReply();
        }
    }
}

//  ##########################################################################
