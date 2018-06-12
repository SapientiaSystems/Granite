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
        [Authorize(Roles = "GraniteGuest")]
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
        [Authorize(Roles = "GraniteGuest")]
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
