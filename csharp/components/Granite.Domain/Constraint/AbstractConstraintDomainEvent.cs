//  ##########################################################################
//  # File Name: AbstractConstraintDomainEvent.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.Shared;
using System;

namespace Granite.Domain.Constraint
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class AbstractConstraintDomainEvent:
        AbstractDomainEvent<IConstraint>,
        IConstraintDomainEvent
    {
        public IUserIdProvider    User { get; protected set; }
        public IConstraintResult  Result { get; protected set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected
        AbstractConstraintDomainEvent(
            string             name,
            IConstraint        source,
            IUserIdProvider    user,
            IConstraintResult  result):
            base(name,source)
        {
            User   = user;
            Result = result;
        }
    }
}

//  ##########################################################################
