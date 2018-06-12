//  ##########################################################################
//  # File Name: IConstraintDomainEvent.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.Shared;
using System;

namespace Granite.Domain.Constraint
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface IConstraintDomainEvent:
        IDomainEvent<IConstraint>
    {
        IUserIdProvider    User { get; }
        IConstraintResult  Result { get; }
    }
}

//  ##########################################################################
