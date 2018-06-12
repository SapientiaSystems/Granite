//  ##########################################################################
//  # File Name: NullConstraint.cs
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
    class NullConstraint:
        AbstractDomainEventSource<
            IConstraint,
            IConstraintDomainEvent,
            IConstraintDomainEventObserver>,
        IConstraint
    {
        public string ConstraintId { get { return "NullConstraint"; } }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public IConstraintResult 
        ApplyTo(IUserIdProvider user)
        {
            return new NullConstraintResult();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override IConstraint 
        GetSelf()
        {
            return this;
        }
    }
}

//  ##########################################################################
