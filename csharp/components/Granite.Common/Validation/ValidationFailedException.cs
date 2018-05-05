//  ##########################################################################
//  # File Name: ValidationException.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;

namespace Granite.Common.Validation
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Indicates a failure in validation rules.
    /// </summary>
    ///  
    public
    class ValidationFailedException:
        Exception
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        ValidationFailedException() {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        ValidationFailedException(string message) :
            base(message)
        { }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        ValidationFailedException(string format,params object[] arguments):
            base(string.Format(format,arguments)) {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        ValidationFailedException(string message,Exception cause):
            base(message,cause) {}
    }
}

//  ##########################################################################
