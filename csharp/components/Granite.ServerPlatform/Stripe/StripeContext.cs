//  ##########################################################################
//  # File Name: StripeContext.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Stripe;
using System.Configuration;

namespace Granite.ServerPlatform.Stripe
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class StripeContext:
        IStripeContext
    {
        public bool IsAuthenticated { get; protected set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        StripeContext()
        {
            IsAuthenticated = false;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void 
        Authenticate()
        {
            string apiKey = 
                ConfigurationManager.AppSettings["StripeApiKey"];

            StripeConfiguration.SetApiKey(apiKey);
            IsAuthenticated = true;
        }
    }
}

//  ##########################################################################
