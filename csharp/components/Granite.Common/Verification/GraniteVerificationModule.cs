//  ##########################################################################
//  # File Name: GraniteVerificationModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Injection;
using System.Configuration;
using System.Net;
using System.Net.Mail;
using SystemEx.Injection;

namespace Granite.Common.Verification
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class GraniteVerificationModule:
        VerificationModule
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        public
        GraniteVerificationModule():
            base("GraniteVerificationModule") {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        protected override IProvider<IVerificationMessageGenerator> 
        GetVerificationMessageGeneratorProvider()
        {
            return
                new FuncBasedProvider<IVerificationMessageGenerator>(
                    () => {return new HtmlVerificationMessageGenerator();});
        }
    }
}

//  ##########################################################################
