//  ##########################################################################
//  # File Name: UtilityModule.cs
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
    class UtilityModule:
        AbstractModule
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        public
        UtilityModule():
            base("GraniteUtilityModule") {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override void
        Initialize()
        {
            Bind<IEmailSession>()
                .ToProvider(
                    new FuncBasedProvider<IEmailSession>(
                        () => 
                        {
                            SmtpClient client =
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

                            return new SmtpEmailSession(client);
                        } ) )
                .WithScope<RequestScope>();
            Bind<IEmailGenerator>()
                .ToType<HtmlEmailGenerator>()
                .WithScope<RequestScope>();
            Bind<IEmailSender>()
                .ToType<EmailSender>()
                .WithScope<RequestScope>();
            Bind<IVerificationCodeGenerator>()
                .ToType<VerificationCodeGenerator>()
                .WithScope<RequestScope>();
        }
    }
}

//  ##########################################################################
