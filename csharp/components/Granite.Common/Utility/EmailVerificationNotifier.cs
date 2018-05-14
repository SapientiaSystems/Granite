//  ##########################################################################
//  # File Name: EmailVerificationNotifier.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using System.Net.Mail;
using System.Net.Mime;
using System.Reflection;
using SystemEx.Injection;

namespace Granite.Common.Utility
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class EmailVerificationNotifier:
        IVerificationNotifier
    {
        private SmtpClient                    itsClient;
        private IVerificationMessageGenerator itsGenerator;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        EmailVerificationNotifier(
            SmtpClient                    client,
            IVerificationMessageGenerator generator)
        {
            itsClient    = client;
            itsGenerator = generator;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void 
        Notify(VerificationNotice notice)
        {
            Assembly       assembly = Assembly.GetExecutingAssembly();
            MailMessage    mail     = new MailMessage();
            AlternateView  html     = null;               
            LinkedResource image    = null;

            html = 
                AlternateView.CreateAlternateViewFromString(
                    GenerateMessage(notice),
                    null,
                    "text/html");
            image =
                new LinkedResource(
                    assembly.GetManifestResourceStream(
                        "Granite.Common.Resources.Logo.gif"),
                    MediaTypeNames.Image.Gif);

            mail.IsBodyHtml = true;
            image.ContentId = "logo";
            html.LinkedResources.Add(image);

            mail.AlternateViews.Add(html);
            mail.From = new MailAddress(notice.From);
            mail.To.Add(new MailAddress(notice.To));
            mail.Subject = GetSubject(notice);

            itsClient.Send(mail);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected string
        GenerateMessage(VerificationNotice notice)
        {
            switch (notice.MessageType)
            {
            case MessageKind.CONFIRM_NEW_USER_EMAIL:
                return itsGenerator.GenerateForNewUserEmail(notice.Parameters);
            case MessageKind.CONFIRM_UPDATED_USER_EMAIL:
                return itsGenerator.GenerateForUpdatedUserEmail(notice.Parameters);
            }

            throw new ArgumentException("unknown message type");
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected string
        GetSubject(VerificationNotice notice)
        {
            switch (notice.MessageType)
            {
            case MessageKind.CONFIRM_NEW_USER_EMAIL:
                return "EzValuator: New User Email Confirmation";
            case MessageKind.CONFIRM_UPDATED_USER_EMAIL:
                return "EzValuator: Updated User Email Confirmation";
            }

            throw new ArgumentException("unknown message type");
        }
    }
}

//  ##########################################################################
