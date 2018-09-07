//  ##########################################################################
//  # File Name: IConstraintResult.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################


namespace Granite.Domain.Constraint
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface IConstraintResult
    {
        IConstraint Source { get; }

        bool        IsSatisfied { get; }

        string      SuccessMessage { get; }
        string      FailureMessage { get; }
    }
}

//  ##########################################################################
