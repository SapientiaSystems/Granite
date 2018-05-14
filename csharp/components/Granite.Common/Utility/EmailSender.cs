//  ##########################################################################
//  # File Name: EmailSender.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.Collections.Generic;
using System.Configuration;
using System.Net;
using System.Net.Mail;
using System.Net.Mime;
using System.Reflection;
using Strata.Foundation.Email;
using SystemEx.Injection;

namespace Granite.Common.Utility
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class EmailSender:
        IEmailSender
    {
        private IEmailSession   itsSession;
        private IEmailGenerator itsGenerator;
        private SmtpClient      itsClient;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        EmailSender(IEmailSession session,IEmailGenerator generator)
        {
            itsSession = session;
            itsGenerator = generator;
            itsClient =
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

        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void 
        GenerateAndSend(
            string                     from,
            string                     to,
            IDictionary<string,string> parameters)
        {
            /*
            IEmailMessage message = itsSession.CreateEmailMessage(from,to);

            message.IsBodyHtml = true;
            message.Body = itsGenerator.Generate(parameters);
            itsSession.Send(message);
            */
            MailMessage   mail = new MailMessage();
            AlternateView htmlView = 
                AlternateView.CreateAlternateViewFromString(
                    itsGenerator.Generate(parameters),
                    null,
                    "text/html");
            Assembly       assembly = Assembly.GetExecutingAssembly();
            LinkedResource image
                = new LinkedResource(
                    assembly.GetManifestResourceStream("Granite.Common.Resources.Logo.gif"),
                    MediaTypeNames.Image.Gif);

            mail.IsBodyHtml = true;
            image.ContentId = "logo";
            htmlView.LinkedResources.Add(image);

            mail.AlternateViews.Add(htmlView);
            mail.From = new MailAddress("verification@ezvaluator.com");
            mail.To.Add(new MailAddress(to));
            itsClient.Send(mail);
        }
    }
}

//  ##########################################################################
