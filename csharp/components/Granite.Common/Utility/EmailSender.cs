//  ##########################################################################
//  # File Name: EmailSender.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.Collections.Generic;
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
            IEmailMessage message = itsSession.CreateEmailMessage(from,to);

            message.IsBodyHtml = true;
            message.Body = itsGenerator.Generate(parameters);
            itsSession.Send(message);
        }
    }
}

//  ##########################################################################
