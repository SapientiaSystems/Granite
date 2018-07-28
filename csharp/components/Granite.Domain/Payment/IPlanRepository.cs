//  ##########################################################################
//  # File Name: IPlanRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Domain.Repository;
using System;

namespace Granite.Domain.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IPlanRepository:
        IRepository<Guid,IPlan>
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        IPlan
        GetUniqueWithPlanName(string planName);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        bool
        HasUniqueWithPlanName(string planName);
    }
}

//  ##########################################################################
