//  ##########################################################################
//  # File Name: AbstractConstraintResult.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;

namespace Granite.Domain.Constraint
{
    //////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public abstract
    class AbstractConstraintResult:
        IConstraintResult
    {
        public bool   IsSatisfied { get; protected set; }

        public string SuccessMessage { get; protected set; }
        public string FailureMessage { get; protected set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected
        AbstractConstraintResult(bool isSatisfied,string message)
        {
            IsSatisfied = isSatisfied;

            if (IsSatisfied)
                SuccessMessage = message;
            else
                FailureMessage = message;
        }
    }
}

//  ##########################################################################
