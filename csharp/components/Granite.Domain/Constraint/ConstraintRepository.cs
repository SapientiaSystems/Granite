//  ##########################################################################
//  # File Name: ConstraintRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using SystemEx.Injection;

namespace Granite.Domain.Constraint
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class ConstraintRepository:
        IConstraintRepository
    {
        private IReadOnlyDictionary<string,IConstraint> itsConstraints;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        ConstraintRepository(IProvider<IList<IConstraint>> provider)
        {
            IDictionary<string,IConstraint> temp = 
                new ConcurrentDictionary<string,IConstraint>();

            foreach (IConstraint constraint in provider.Get())
                temp.Add(constraint.ConstraintId,constraint);

            itsConstraints = new ReadOnlyDictionary<string,IConstraint>(temp);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public IConstraint 
        GetConstraint(string constraintId)
        {
            if (HasConstraint(constraintId))
                return itsConstraints[constraintId];

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public ISet<IConstraint>
        GetConstraints(string[] constraintIds)
        {
            ISet<IConstraint> constraints = 
                new HashSet<IConstraint>(itsConstraints.Values);

            foreach (string constraintId in constraintIds)
                if (HasConstraint(constraintId))
                    constraints.Add(itsConstraints[constraintId]);
           
            return constraints;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public ISet<IConstraint>
        GetConstraints()
        {
            return new HashSet<IConstraint>(itsConstraints.Values);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public bool 
        HasConstraint(string constraintId)
        {
            return itsConstraints.ContainsKey(constraintId);
        }
    }
}

//  ##########################################################################
