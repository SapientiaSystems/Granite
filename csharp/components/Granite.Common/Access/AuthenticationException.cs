//  ##########################################################################
//  # File Name: AuthenticationException.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;

namespace Granite.Common.Access
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Indicates a failure in authentication.
    /// </summary>
    ///  
    public
    class AuthenticationException:
        Exception
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        AuthenticationException() {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        AuthenticationException(string message) :
            base(message)
        { }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        AuthenticationException(string format,params object[] arguments):
            base(string.Format(format,arguments)) {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        AuthenticationException(string message,Exception cause):
            base(message,cause) {}
    }
}

//  ##########################################################################
