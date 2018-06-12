//  ##########################################################################
//  # File Name: NullConstraintResult.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;

namespace Granite.Domain.Constraint
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class NullConstraintResult:
        AbstractConstraintResult
    {
        public 
        NullConstraintResult():
            base(
                true,
                "NullContraint: this constraint is always satisfied.") {}
    }
}

//  ##########################################################################
