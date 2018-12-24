//  ##########################################################################
//  # File Name: VerificationModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Ninject.Activation;
using Strata.Ninject.Injection;
using System.Configuration;
using System.Net;
using System.Net.Mail;

namespace Granite.Common.Verification
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class VerificationModule<T>:
        AbstractModule
        where T:IVerificationMessageGenerator
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        public
        VerificationModule() {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override void
        Load()
        {
            Bind<SmtpClient>()
                .ToMethod(
                    (IContext c) =>
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
                    })
                .InDefaultScope();

            Bind<IVerificationMessageGenerator>()
                .To<T>()
                .InDefaultScope();

            Bind<IVerificationNotifier>()
                .To<EmailVerificationNotifier>()
                .InDefaultScope();

            Bind<IVerificationCodeGenerator>()
                .To<VerificationCodeGenerator>()
                .InDefaultScope();
        }
    }
}

//  ##########################################################################
