//  ##########################################################################
//  # File Name: MockEmailSender.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Value;
using Granite.ProvidedService.User;
using System;
using System.Collections.Generic;

namespace Granite.Common.Utility
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class FakeEmailSender:
        IVerificationNotifier
    {
        public VerifyUserRequest Request { get; protected set; }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void 
        Notify(VerificationNotice notice)
        {
            IDictionary<string,string> parameters = notice.Parameters;

            Request =
                new VerifyUserRequest()
                {
                    CorrelationId =
                        Guid.Parse(parameters[Constant.CORRELATION_ID_PROPERTY]),
                    UserId = 
                        Guid.Parse(parameters[Constant.USER_ID_PROPERTY]),
                    UserNameToVerify =
                        parameters[Constant.USER_NAME_PROPERTY],
                    VerificationCode = 
                        parameters[Constant.VERIFICATION_CODE_PROPERTY]
                };
        }
    }
}

//  ##########################################################################
