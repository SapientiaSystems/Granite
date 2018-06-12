//  ##########################################################################
//  # File Name: IConstraintRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.Collections.Generic;

namespace Granite.Domain.Constraint
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IConstraintRepository
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IConstraint
        GetConstraint(string constraintName);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ISet<IConstraint>
        GetConstraints(string[] constraintNames);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        ISet<IConstraint>
        GetConstraints();

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        HasConstraint(string constraintName);
    }
}

//  ##########################################################################
