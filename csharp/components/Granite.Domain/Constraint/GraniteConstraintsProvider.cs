//  ##########################################################################
//  # File Name: GraniteConstraintsProvider.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Utility;
using System;
using System.Collections.Generic;
using SystemEx.Injection;

namespace Granite.Domain.Constraint
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public 
    class GraniteConstraintsProvider:
        IProvider<IList<IConstraint>>
    {

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        GraniteConstraintsProvider()
        {
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public IList<IConstraint> 
        Get()
        {
            return GetApplicationConstraints();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual IList<IConstraint>
        GetApplicationConstraints()
        {
            return new List<IConstraint>();
        }
    }
}

//  ##########################################################################
