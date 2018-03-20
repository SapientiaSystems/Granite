//  ##########################################################################
//  # File Name: IStripeContext.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.Security;

namespace Granite.ServerPlatform.Stripe
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IStripeContext
    {
        bool IsAuthenticated { get;  }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        Authenticate();
    }
}

//  ##########################################################################
