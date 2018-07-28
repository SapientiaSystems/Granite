//  ##########################################################################
//  # File Name: IPaymentService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Access;
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
        [Authorize(Roles = "GraniteAdministrator")]
        CreatePlanReply
        CreatePlan(CreatePlanRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteAdministrator")]
        DeletePlanReply
        DeletePlan(DeletePlanRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteUser")]
        CreateAccountReply
        CreateAccount(CreateAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteUser")]
        UpdateAccountReply
        UpdateAccount(UpdateAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteUser")]
        DeleteAccountReply
        DeleteAccount(DeleteAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteUser")]
        GetAccountReply
        GetAccount(GetAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteUser")]
        CreateSubscriptionReply
        CreateSubscription(CreateSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteUser")]
        UpdateSubscriptionReply
        UpdateSubscription(UpdateSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteUser")]
        DeleteSubscriptionReply
        DeleteSubscription(DeleteSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteUser")]
        GetSubscriptionReply
        GetSubscription(GetSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteUser")]
        MakePaymentReply
        MakePayment(MakePaymentRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteAdministrator")]
        MakeRefundReply
        MakeRefund(MakeRefundRequest request);
    }
}

//  ##########################################################################
