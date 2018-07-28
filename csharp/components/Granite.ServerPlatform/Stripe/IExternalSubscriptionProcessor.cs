//  ##########################################################################
//  # File Name: IExternalSubscriptionProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.RequiredService.Payment;
using System;

namespace Granite.ServerPlatform.Stripe
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface IExternalSubscriptionProcessor
    {

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        CreateExternalSubscriptionReply
        CreateExternalSubscription(CreateExternalSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        UpdateExternalSubscriptionReply
        UpdateExternalSubscription(UpdateExternalSubscriptionRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        DeleteExternalSubscriptionReply
        DeleteExternalSubscription(DeleteExternalSubscriptionRequest request);
    }
}

//  ##########################################################################
