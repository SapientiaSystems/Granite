//  ##########################################################################
//  # File Name: PaymentApplicationModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Payment;
using Strata.Foundation.Injection;
using System;

namespace Granite.Application.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class PaymentApplicationModule:
        AbstractModule
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        public
        PaymentApplicationModule() :
            base("PaymentApplicationModule") {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override void 
        Initialize()
        {
            Bind<IPaymentService>()
                .ToType<PaymentService>()
                .WithScope(DefaultScope);
        }
    }
}

//  ##########################################################################
