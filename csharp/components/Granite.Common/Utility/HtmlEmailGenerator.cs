//  ##########################################################################
//  # File Name: HtmlEmailGenerator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Value;
using System.Collections.Generic;
using System.Text;

namespace Granite.Common.Utility
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class HtmlEmailGenerator:
        IEmailGenerator
    {
        public string 
        Generate(IDictionary<string,string> parameters)
        {
            StringBuilder builder = new StringBuilder();

            CheckParameters(
                parameters,
                Constant.USER_NAME_PROPERTY,
                Constant.VERIFICATION_CODE_PROPERTY);

            builder
                .Append("<html>")
                .Append("To complete user identity setup request for user name: ")
                .Append(parameters[Constant.USER_NAME_PROPERTY])
                .Append(" please confirm email address with verification code: ")
                .Append(parameters[Constant.VERIFICATION_CODE_PROPERTY])
                .Append(".")
                .Append("<p/>")
                .Append("<p/>")
                .Append("<a href=\"en.wikipedia.org\">Confirm Email Address</a>")
                .Append("</html>");

            return builder.ToString();
        }

        private void 
        CheckParameters(
            IDictionary<string,string> parameters,
            string                     userNameKey,
            string                     verificationCodeKey)
        {
            
        }
    }
}

//  ##########################################################################
