//  ##########################################################################
//  # File Name: HtmlVerificationMessageGenerator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Value;
using System.Collections.Generic;
using System.Text;

namespace Granite.Common.Verification
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class HtmlVerificationMessageGenerator:
        IVerificationMessageGenerator
    {
        public string 
        GenerateForNewUserEmail(IDictionary<string,string> parameters)
        {
            StringBuilder builder = new StringBuilder();
            
            CheckParameters(
                parameters,
                Constant.USER_NAME_PROPERTY,
                Constant.VERIFICATION_CODE_PROPERTY);

            builder
                .Append("<html>")
                .Append(
                @"
                <div 
                    style =
                       ""background-color: lightgray;
                        height: 60px;
                        vertical-align: middle;
                        padding: 10px;"">
                    <img
                        style=
                          ""display: inline-block;
                            height: 60px;
                            width: 60px;
                            vertical-align: middle;""
                        src=""cid:logo""/>
                    <div
                        style=
                          ""display: inline-block;
                            height: 60px;
                            line-height: 60px;
                            color: white;
                            vertical-align: middle;
                            font-size: 32pt;
                            padding-left: 5px;"">EzValuator</div> 
                </div>")
                .Append(string.Empty)
                .Append("<span style=\"font-size: 18pt;color: #242424;\">To complete EzValuator registration for user '")
                .Append(parameters[Constant.USER_NAME_PROPERTY])
                .Append("' please confirm email address by clicking on the link: </span>")
                .Append("<a style=\"font-size: 18pt;\" href=\"https://ezvaluatorweb.azurewebsites.net/#/verification?userId=")
                .Append(parameters[Constant.USER_ID_PROPERTY])
                .Append("&userName=")
                .Append(parameters[Constant.USER_NAME_PROPERTY])
                .Append("&confirmationCode=")
                .Append(parameters[Constant.VERIFICATION_CODE_PROPERTY])
                .Append("\">Confirm Email Address</a>")
                .Append("</html>");

            return builder.ToString();
        }

        public string
        GenerateForUpdatedUserEmail(IDictionary<string,string> parameters)
        {
            StringBuilder builder = new StringBuilder();

            CheckParameters(
                parameters,
                Constant.USER_NAME_PROPERTY,
                Constant.VERIFICATION_CODE_PROPERTY);

            builder
                .Append("<html>")
                .Append(
                @"
                <div 
                    style =
                       ""background-color: lightgray;
                        height: 60px;
                        vertical-align: middle;
                        padding: 10px;"">
                    <img
                        style=
                          ""display: inline-block;
                            height: 60px;
                            width: 60px;
                            vertical-align: middle;""
                        src=""cid:logo""/>
                    <div
                        style=
                          ""display: inline-block;
                            height: 60px;
                            line-height: 60px;
                            color: white;
                            vertical-align: middle;
                            font-size: 32pt;
                            padding-left: 5px;"">EzValuator</div> 
                </div>")
                .Append(string.Empty)
                .Append("<span style=\"font-size: 18pt;color: #242424;\">The email address for user '")
                .Append(parameters[Constant.USER_NAME_PROPERTY])
                .Append("' was changed. Please confirm email address by clicking on the link: </span>")
                .Append("<a style=\"font-size: 18pt;\" href=\"https://ezvaluatorweb.azurewebsites.net/#/verification?userId=")
                .Append(parameters[Constant.USER_ID_PROPERTY])
                .Append("&userName=")
                .Append(parameters[Constant.USER_NAME_PROPERTY])
                .Append("&confirmationCode=")
                .Append(parameters[Constant.VERIFICATION_CODE_PROPERTY])
                .Append("\">Confirm Email Address</a>")
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
