//  ##########################################################################
//  # File Name: AbstractConstraint.cs
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
    public abstract
    class AbstractConstraint:
        AbstractDomainEventSource<
            IConstraint,
            IConstraintDomainEvent,
            IConstraintDomainEventObserver>,
        IConstraint
    {
        public virtual string ConstraintId { get; protected set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected
        AbstractConstraint(string constraintName)
        {
            ConstraintId = constraintName;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public IConstraintResult 
        ApplyTo(IUserIdProvider user)
        {
            IConstraintDomainEvent evnt = DoApplyTo(user);

            Notify(evnt);
            return evnt.Result;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected abstract IConstraintDomainEvent
        DoApplyTo(IUserIdProvider user);
    }
}

//  ##########################################################################
