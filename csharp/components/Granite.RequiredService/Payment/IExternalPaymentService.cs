//  ##########################################################################
//  # File Name: IExternalPaymentService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.ServiceModel;

namespace Granite.RequiredService.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface IExternalPaymentService
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        CreateExternalAccountReply
        CreateExternalAccount(CreateExternalAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        UpdateExternalAccountReply
        UpdateExternalAccount(UpdateExternalAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        DeleteExternalAccountReply
        DeleteExternalAccount(DeleteExternalAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        CreateExternalSubscriptionReply
        CreateExternalSubscription(CreateExternalSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        UpdateExternalSubscriptionReply
        UpdateExternalSubscription(UpdateExternalSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        DeleteExternalSubscriptionReply
        DeleteExternalSubscription(DeleteExternalSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        MakeExternalPaymentReply
        MakeExternalPayment(MakeExternalPaymentRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        MakeExternalRefundReply
        MakeExternalRefund(MakeExternalRefundRequest request);
    }
}

//  ##########################################################################
