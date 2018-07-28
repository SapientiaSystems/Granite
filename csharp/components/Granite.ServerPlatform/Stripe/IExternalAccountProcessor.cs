//  ##########################################################################
//  # File Name: IExternalAccountProcessor.cs
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
    interface IExternalAccountProcessor
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        CreateExternalAccountReply
        CreateExternalAccount(CreateExternalAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        UpdateExternalAccountReply
        UpdateExternalAccount(UpdateExternalAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        DeleteExternalAccountReply
        DeleteExternalAccount(DeleteExternalAccountRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        GetExternalAccountReply
        GetExternalAccount(GetExternalAccountRequest request);
    }
}

//  ##########################################################################
