//  ##########################################################################
//  # File Name: IPaymentProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Payment;
using System;

namespace Granite.Application.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface IPaymentProcessor
    {

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        CreatePlanReply
        CreatePlan(CreatePlanRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        DeletePlanReply
        DeletePlan(DeletePlanRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        CreateAccountReply
        CreateAccount(CreateAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        UpdateAccountReply
        UpdateAccount(UpdateAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        DeleteAccountReply
        DeleteAccount(DeleteAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        GetAccountReply
        GetAccount(GetAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        CreateSubscriptionReply
        CreateSubscription(CreateSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        UpdateSubscriptionReply
        UpdateSubscription(UpdateSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        DeleteSubscriptionReply
        DeleteSubscription(DeleteSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        GetSubscriptionReply
        GetSubscription(GetSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        MakePaymentReply
        MakePayment(MakePaymentRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        MakeRefundReply
        MakeRefund(MakeRefundRequest request);
    }
}

//  ##########################################################################
