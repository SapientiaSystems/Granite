//  ##########################################################################
//  # File Name: VerificationFailedException.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;

namespace Granite.Application.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [Serializable]
    public
    class VerificationFailedException:
        Exception
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        VerificationFailedException(
            Guid userId,
            string expectedCode,
            string actualCode) :
            base(
                string.Format(
                    "Verification for userId '{0}' failed: " +
                    "expected code <{1}> but received code <{2}>.",
                    userId.ToString(),
                    expectedCode,
                    actualCode)) {}
    }
}

//  ##########################################################################
