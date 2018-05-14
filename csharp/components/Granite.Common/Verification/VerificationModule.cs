//  ##########################################################################
//  # File Name: VerificationModule.cs
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
    public abstract
    class VerificationModule:
        AbstractModule
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        public
        VerificationModule(string name):
            base(name) {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override void
        Initialize()
        {
            Bind<SmtpClient>()
                .ToProvider(
                    new FuncBasedProvider<SmtpClient>(
                        () => 
                        {
                            return
                                new SmtpClient()
                                {
                                    Host = "smtp.gmail.com",
                                    Port = 587,
                                    EnableSsl = true,
                                    Timeout = 10000,
                                    DeliveryMethod = SmtpDeliveryMethod.Network,
                                    TargetName = "STARTTLS/smtp.gmail.com",
                                    UseDefaultCredentials = false,
                                    Credentials = 
                                        new NetworkCredential(
                                            ConfigurationManager.AppSettings["GmailUser"],
                                            ConfigurationManager.AppSettings["GmailPassword"])
                                };
                        } ) )
                .WithScope(DefaultScope);
            Bind<IVerificationMessageGenerator>()
                .ToProvider(GetVerificationMessageGeneratorProvider())
                .WithScope(DefaultScope);
            Bind<IVerificationNotifier>()
                .ToType<EmailVerificationNotifier>()
                .WithScope(DefaultScope);
            Bind<IVerificationCodeGenerator>()
                .ToType<VerificationCodeGenerator>()
                .WithScope(DefaultScope);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        protected abstract IProvider<IVerificationMessageGenerator>
        GetVerificationMessageGeneratorProvider();
    }
}

//  ##########################################################################
