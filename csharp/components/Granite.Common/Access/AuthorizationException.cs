//  ##########################################################################
//  # File Name: ValidationException.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;

namespace Granite.Common.Access
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Indicates a failure in authorization.
    /// </summary>
    ///  
    public
    class AuthorizationException:
        Exception
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        AuthorizationException() {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        AuthorizationException(string message) :
            base(message)
        { }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        AuthorizationException(string format,params object[] arguments):
            base(string.Format(format,arguments)) {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        AuthorizationException(string message,Exception cause):
            base(message,cause) {}
    }
}

//  ##########################################################################
