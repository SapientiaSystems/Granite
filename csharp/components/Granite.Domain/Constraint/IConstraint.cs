//  ##########################################################################
//  # File Name: IConstraint.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.Shared;

namespace Granite.Domain.Constraint
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IConstraint:
        IDomainEventSource<
            IConstraint,
            IConstraintDomainEvent,
            IConstraintDomainEventObserver>
    {
        string ConstraintId { get; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IConstraintResult
        ApplyTo(IUserIdProvider user);
    }
}

//  ##########################################################################
