//  ##########################################################################
//  # File Name: IPaymentService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.ServiceModel;

namespace Granite.ProvidedService.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [ServiceContract]
    public
    interface IPaymentService
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        CreateAccountReply
        CreateAccount(CreateAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        UpdateAccountReply
        UpdateAccount(UpdateAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        DeleteAccountReply
        DeleteAccount(DeleteAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        CreateSubscriptionReply
        CreateSubscription(CreateSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        UpdateSubscriptionReply
        UpdateSubscription(UpdateSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        DeleteSubscriptionReply
        DeleteSubscription(DeleteSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        MakePaymentReply
        MakePayment(MakePaymentRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        MakeRefundReply
        MakeRefund(MakeRefundRequest request);
    }
}

//  ##########################################################################
