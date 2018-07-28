//  ##########################################################################
//  # File Name: IExternalPlanProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.RequiredService.Payment;
using System;

namespace Granite.ServerPlatform.Stripe
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IExternalPlanProcessor
    {

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        CreateExternalPlanReply
        CreateExternalPlan(CreateExternalPlanRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        DeleteExternalPlanReply
        DeleteExternalPlan(DeleteExternalPlanRequest request);
    }
}

//  ##########################################################################
