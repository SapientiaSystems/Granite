//  ##########################################################################
//  # File Name: MockUtilityModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Email;
using Strata.Foundation.Injection;
using System.Configuration;
using System.Net;
using System.Net.Mail;

namespace Granite.Common.Utility
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class MockUtilityModule:
        AbstractModule
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        public
        MockUtilityModule():
            base("MockUtilityModule") {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override void
        Initialize()
        {
            Bind<IEmailSender>()
                .ToType<MockEmailSender>()
                .WithScope<RequestScope>();
            Bind<IVerificationCodeGenerator>()
                .ToType<VerificationCodeGenerator>()
                .WithScope<RequestScope>();
        }
    }
}

//  ##########################################################################
